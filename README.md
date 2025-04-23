# bookingTripRequest-agency

This app represents an agency that searches for flights and accommodations via the
prompt from the user through a HTTP call. 

It's composed by a LLM (Anthropic) and tools to find flights, accommodations and sending mails. 

Once a search is requested the app will look for the flights, accommodations, 
and will email the requester with some options and the best value offer. 

## Prerequisites

Start a local email service. 
```shell
docker run -d -p 1025:1025 -p 8025:8025 mailhog/mailhog
```
Note: Sending emails has only been tested when running in local. 

## Call the service 

```shell
curl http://localhost:9000/trip/search \
-H "Content-Type: application/json" \
-d '{"question": "find a bookingTripRequest from seoul to tokyo and back, from 2025-05-07 to 2025-05-14 The flight price not higher than 300 total and the total accomodation for the week not higher than 600. Send the suggestion to 'test.user@gmail.com'"
}'
```

The result (in `localhost:8025`) should be something like: 

![mail_header.png](mail_header.png)
![mail_center.png](mail_center.png)
![mail_bottom.png](mail_bottom.png)

Possible routes to extend the example: 
- improve prompt https://docs.spring.io/spring-ai/reference/api/prompt.html and include Patrik suggestions.
- Pre-reserve best value trip after request and send an email with that reservation to user 
- using timers to check if the flights/accommodations are still available in the market (fake must be then refactored to use a real/external endpoint)
- using views to check if flights already exist for the requested dates/constraints

Afterthoughts:
- How can we do integration tests without consuming LLM credit?
- Tools might need state to keep away the LLM from misusing them. For example, using them more often than expected. 
  - In this regard, ESE might be well suited to not only keep the previous conversations but the usage of tools. 