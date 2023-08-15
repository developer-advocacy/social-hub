# README

This will be a new unified social hub for all my services. _Except_, it won't. It'll delegate
to [Ayrshare](https://ayrshare.com).

## Design

This is just a resource server, from the OAuth perspective. There will be a gateway that'll act as a token relay, with
which you must be authenticated to obtain a token to then postRequest requests to this service.

See, eg, the implementation of [the This Week In Gateway service ](https://github.com/this-week-in/studio-gateway) for
how to setup this gateway service to also be an OAuth client and resource server that rejects requests that don't have a
valid token in the request.

Each user is mapped to one or more social media types (these should correspond to the social media services supported by
Ayr itself). Each user must in turn have an access code mapped to Ayr. It's ok if the same OAuth user has mutliple
mappings to the same Ayr account. So, one might imagine three tables:

### HUB_USERS

This table will contain a username that shall line up with the username given by the OAuth
authenticated `Principa#getName()`.

* `name` - the user name
* `id`
* `created_date` when the user was created

### HUB_USERS_AYRSHARE_ACCOUNTS

This is a mapping of ayrshare accounts available to a given user

* `id` the ID of the `ayrshare_account`
* `ayrshare_accounts_fk`
* `user_fk`

### HUB_AYRSHARE_ACCOUNTS

Ayrshare permits only one social media connection for each social media account, per paying account. So, if we pay $100
a month for an Ayrshare account, it in turn can only be used to tweet to _one_ twitter account. So if I wanted to tweet
to both `@starbuxman` _and_ `@CoffeeSoftwareShow`, then I'd need to buy _two_ Ayrshare accounts, for $100. Supposing I
had two Ayrshare accounts, account `A` and `B`, I could use the same account to send messages to many different social
media sites, including Twitter, LinkedIn, Facebook, Instagram, etc., but only if there is only one Twitter per account.
So, I might have one `ayrshare_account`, with a `label` for ease of contextualization. Account `A` might have the
label `joshlong`. This would be the account I'll use to tweet, linkedin, instagram stuff related to my professional
work. Account `B` might have the label `springcentral` which I could use to tweet or postRequest to LinkedIn for all things
Spring.

* `label`
* `bearer token`
* `id`
* `created_date`

### HUB_POSTS_TARGET_PLATFORMS

Each postRequest must have one or more records in this table telling the engine how, in effect, to route the message. Should it
be sent to one particular Ayrshare account r another? And to which social media platform within that Ayrshare account.
It's important that the engine try to craft only ONE unique message for each Ayrshare account. so, if a postRequest is supposed
to be sent on Ayrshare account A (`twitter`, `facebook`), and on Ayrshare account B (`instagram`, `linkedin`), then it's
important that this result in only _two_ calls to the Ayrshare HTTP APi, not four. We can specify multiple target
platforms per HTTP call, and it only decrements once from the quota consumption.

* `id`
* `ayrshare_account_fk` - the foreign key into the `hub_ayrshare_account`
* `post_fk` - the foreign key of the `hub_post`
* `platform` - the platform within a given Ayrshare account we'd like to postRequest this. One of an enumerated set of values (
  like 'twitter', 'instagram', etc.)

### HUB_POSTS

this contains the definition of a postRequest, the bytes for any media to be attached, the scheduled date, the request date,
etc.

* `text` - the text of the postRequest to make. Try to be mindful that you should take no longer than the maximum allowed in
  the social media service with the most restrictive postRequest lengths. This will probably be twitter.
* `id`
* `date` - the date when the record was created in the database
* `scheduled_date` - Ayrshare can schedule posts in the future. This field is nullable, but if specified should be sent
  to Ayrshare so it can properly schedule the postRequest.

### HUB_MEDIA

We'll persist the bytes and make them available through a public URL which we can then send in the request

* `id`
* `uuid` - this should be the UUID that is used to create the bookmarkable link to the media from our service
* `content` - a blob of bytes
* `order` - the order within a grouping of postRequest media. e.g.: if you create four images to be attached to a postRequest, you
  should order them one, two, three, etc., with an integer value here.
* `content-type`

### HUB_POSTS_MEDIA

Contains the links and ordering of media to posts

* `id`
* `post_fk` - a URL identifying where we found it
* `media_fk` - the foreign key to the `hub_media` table
* 
