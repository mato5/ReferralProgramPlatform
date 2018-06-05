## Referral Program Platform

### Project description :

A web application to manage referral programs. Referral programs enable us to send invitation links to potential users of some software (i.e. target application). Such a user should be, upon registration, able to send further invitations according to the configuration of the particular program. The platform should also maintain a waiting list of people interested in using the target application. The platform should allow us to add more users from the waiting list, spread new invitations or setting the number of referrals an existing user is able to send (both individually and in batches). We should be able to visualize the data about referrals in a tree chart, selecting according to geo-location, unused referrals, etc. 

### Installation and Usage :

[Preconfigured WildFly](https://drive.google.com/open?id=1SFbd3eC28T5eB8DZhlVivluO4xmqsYQD)<br>
WildFly credentials:<br>
name: admin<br>
password: admin<br>

### Requirements :

Java SE 9 or later

### Communication with the target application :

#### Request

First make sure that WildFly is available to external clients and the *CORS* (Cross-Origin Resource Sharing) is enabled. To receive information about the user's role in a specific program, send a PUT request to : <br> <br>
`{SERVER_ADDRESS_GOES_HERE}/platform/api/programs/{PROGRAM_NAME_GOES_HERE}/role` <br> <br>
**This request MUST contain user's email in its body without any additional characters!**

#### Response

The response is in *JSON* format and contains user's role based on the program name and email specified in the request. <br>
A single user can participate in a program as:
 * **ADMINISTRATOR** - user is the owner of a program, can manage it and has access to the program's applications
 * **WAITING** - user is registered on the program's waiting list and is awaiting an invitation
 * **CUSTOMER** - user is participating in the program and has access to the program's applications
 * **NONE** - user is unknown to the program and as such should not have access to the program's applications

#### Example

The following *cURL* command sends a **request** asking for a role of the user with an email : **mary@domain.com** in the program named **Test Program** <br>
``` 
  curl -X PUT \
  http://localhost:8080/platform/api/programs/Test%20Program/role \
  -H 'Cache-Control: no-cache' \
  -H 'Content-Type: application/json' \
  -d mary@domain.com 
  ```
  
With the following **response** :
```
{
    "role": "ADMINISTRATOR"
}
```
