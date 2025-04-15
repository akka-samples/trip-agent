# trip-agency

## Prerequisites

Start a local email service
```shell
docker run -d -p 1025:1025 -p 8025:8025 mailhog/mailhog
```


Try caching data if using Gatling

Fix the flights and accommodation so the range of the question will be always 7/May/2025 to 14/May/2025
Plan for three questions: 
I'm in seoul and I want to spend a from 10 to 17 in Tokyo
  answers you have flight x 10 and y 17 and accommodation z from 10 to 17
I'm in seoul and I want to spend a from 11 to 17 in Tokyo
  answers you have flight x 10 and y 17 and accommodation z from 10 to 17
   or
  answers you have flight x 12 and y 17 and accommodation z2 from 12 to 17

After the flight to 10 to 17 was picked with accommodation another user could be also getting
the same offer but tries later and the entities are already sold. So it gets denied. 


Request if you didn't the email to send with an id to the workflow to continue. 

Use the coordinator to check for flights, check for the accommodations and if nothings matches show the closest look for something closer.
Then send the info to a queue and provide the link for a workflow that when accepted will book the flight. 
 



using ESE to refer to flights and accomodations
TODO if aim for expansion 
- using timers to check if the flights are still up (using some random )


```shell
curl  http://localhost:9000/trip/search \
-H "Content-Type: application/json" \
-d '{
"id": "abc123",
"locationFrom": "Seoul",
"locationTo": "Tokyo",
"from": "2025-05-07T09:00:00Z",
"to": "2025-05-14T18:00:00Z", "flightMaxPrice": 150, "neighborhood": "any", "accMaxPrice": 600, "email": "test.user@gmail.com"
}' -i
```