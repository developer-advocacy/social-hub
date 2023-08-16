package com.joshlong.socialhub.processor;

import com.joshlong.socialhub.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.integration.amqp.dsl.Amqp;
import org.springframework.integration.context.IntegrationContextUtils;
import org.springframework.integration.core.GenericHandler;
import org.springframework.integration.core.GenericTransformer;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.messaging.MessageChannel;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.messaging.access.intercept.AuthorizationChannelInterceptor;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

@Configuration
class ProcessorIntegrationFlowConfiguration {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final String queue = "socialhub-requests";

    @Bean
    Binding binding() {
        return BindingBuilder//
                .bind(this.queue())//
                .to(this.exchange())//
                .with(this.queue)//
                .noargs();
    }

    @Bean
    Exchange exchange() {
        return ExchangeBuilder.directExchange(this.queue).build();
    }

    @Bean
    Queue queue() {
        return QueueBuilder.durable(this.queue).build();
    }

    @Bean
    JwtAuthenticatingAuthorizationManager jwtAuthenticatingAuthorizationManager(JwtAuthenticationConverter converter, JwtDecoder decoder, UsersService usersService) {
        return new JwtAuthenticatingAuthorizationManager(converter, decoder, usersService);
    }

    @Bean
    AyrshareHttpClient ayrshareClient(HubProperties properties, RestTemplate template) {
        return new AyrshareHttpClient(properties.uri(), template);
    }

    @Bean
    IntegrationFlow inboundAmqpAdapterFlow(ConnectionFactory connectionFactory, MessageChannel inboundPostRequestsMessageChannel) {
        return IntegrationFlow//
                .from(Amqp.inboundAdapter(connectionFactory, this.queue))//
                .channel(inboundPostRequestsMessageChannel)//
                .get();
    }

    @Bean
    MessageChannel inboundPostRequestsMessageChannel(JwtAuthenticatingAuthorizationManager authorizationManager) {
        return MessageChannels.direct().interceptor(new AuthorizationChannelInterceptor(authorizationManager)).getObject();
    }

    @Bean(name = IntegrationContextUtils.ERROR_CHANNEL_BEAN_NAME)
    MessageChannel errorChannel() {
        return MessageChannels.direct().getObject();
    }

    @Bean
    IntegrationFlow errorProcessingFlow() {
        return IntegrationFlow//
                .from(errorChannel())//
                .handle((GenericHandler<Throwable>) (payload, headers) -> {
                    log.error("couldn't process message %s%n", payload);
                    headers.forEach((key, value) -> System.out.println(key + '=' + value));
                    return null;
                })//
                .get();
    }

    @Bean
    IntegrationFlow inboundRequestProcessingFlow(
            AyrshareHttpClient ayrshareHttpClient,
            AyrshareAccountsService accountsService,
            MediaService mediaService,
            PostService postService,
            UsersService usersService,
            MessageChannel inboundPostRequestsMessageChannel,
            PostJsonDecoder postJsonDecoder) {

        record PostRequestAndCredentials(User user, PostRequest postRequest) {
        }

        record PostAndCredentials(User user, Post post) {
        }

        return IntegrationFlow
                .from(inboundPostRequestsMessageChannel)//
                .transform((GenericTransformer<String, PostRequestAndCredentials>) source -> {
                    // 1. take the JSON and turn it into objects
                    var post = postJsonDecoder.decode(source);
                    var user = usersService.userByName(SecurityContextHolder.getContext().getAuthentication().getName());
                    Assert.notNull(post, "the post must not be null");
                    Assert.notNull(user, "the user must not be null");
                    return new PostRequestAndCredentials(user, post);
                })//
                .transform((GenericTransformer<PostRequestAndCredentials, PostAndCredentials>) requestAndCredentials -> {
                    // 2. transform PostRequest into persistent Post
                    var source = requestAndCredentials.postRequest();
                    var platforms = Set.copyOf(Arrays.asList(source.platforms()));
                    var accounts = accountsService.ayrshareAccountsForUser(requestAndCredentials.user().id());
                    var user = requestAndCredentials.user();
                    var targets = new HashMap<AyrshareAccount, Set<String>>();
                    for (var a : accounts)
                        targets.put(a, platforms);

                    var medias = source
                            .media()
                            .entrySet()
                            .stream()
                            .map(entry -> mediaFromKeyAndResource(mediaService, entry.getKey(), entry.getValue()))
                            .toList()
                            .toArray(new Media[0]);

                    var newPost = postService.newPost(
                            requestAndCredentials.user(),
                            medias,
                            targets,
                            source.content(),
                            Instant.now()
                    );

                    return new PostAndCredentials(user, newPost);
                })//
                //todo split ?
                .handle((GenericHandler<PostAndCredentials>) (pac, headers) -> {
                    // 3. take post and issue HTTP requests
                    // todo should this be put in a separate integration flow to handle scheduling? we could just
                    //  pass that along in the request and leave it to Ayrshare, right?

                    var post = pac.post();

                    // todo should i instead do a splitter, splitting across the
                    //  different accounts to which the message should be routed,
                    //  in the integration flow?

                    pac.post().targets().forEach((account, socialPlatforms) -> {
                        var jsonAfterPost = ayrshareHttpClient.post(
                                post.text(),
                                socialPlatforms.toArray(new String[0]),
                                post.media(),
                                account.bearerToken()
                        );
                        log.info("json after post [" + jsonAfterPost + "]");
                    });
                    return null;
                })//
                .get();
    }


    private Media mediaFromKeyAndResource(MediaService mediaService, String key, Resource resource) {
        try {
            return mediaService.newMedia(key, resource.getContentAsByteArray(), MediaType.IMAGE_PNG);
        }//
        catch (Throwable throwable) {
            log.warn("oops!", throwable);//
        }
        return null;
    }
}

