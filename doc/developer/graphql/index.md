---
layout: "default"
toc_levels: 1..3
---

GRAPHQL API
===========
{:.no_toc}

This document describes the GRAPHQL API for IRIDA.

* This comment becomes the toc
{:toc}

Authentication
==============

IRIDA does not allow any un-authenticated interaction with the GRAPHQL API. IRIDA uses [OAuth2](http://oauth.net/2/) for authentication and authorization of clients. Command-line tools must use the password grant type for OAuth2. Our examples are primarily showing how to interact with the IRIDA GRAPHQL API over the using the password flow. IRIDA also supports the authorization code flow, so other web services can interact with IRIDA.

Most programming languages have libraries with convenient interfaces for dealing with OAuth2 authorization. We provide some examples for the programming languages where we've written our own clients, but a comprehensive list of libraries can be found here: <http://oauth.net/code/>


### Altair GraphQL Client
{:.no_toc}

If you want to interact with IRIDA using Altair GraphQL Client you will need to first generate a token using the password flow, using shell and `curl`:

```bash
curl --silent http://localhost:8080/irida/api/oauth/token -X POST -d "client_id=$CLIENT_ID&client_secret=$CLIENT_SECRET&grant_type=password&username=$USERNAME&password=$PASSWORD" | python -m json.tool
```

Install the Altair GraphQL Client in your browser of choice.
* Set url to `http://localhost:8080/api/graphql`
* Set headers
  * Header key: `Authorization`
  * Header value: `bearer TOKEN_FROM_ABOVE`
  
Now you can interact with the api.