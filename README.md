
# Submit VAT Return Frontend

## Summary
This service provides the functionality to submit VAT returns without using external software.

The service can only be used by users who are not subject to MTD (Non MTDfB, Non Digital or MTDfB Exempt) or agents acting on behalf of such a user.

### Running the service
In order to run this microservice, you must have SBT installed.

1. Make sure any dependent services are running using the following service-manager command `sm2 --start VAT_SUBMIT_RETURN_ALL`
2. Stop the frontend in service manager using `sm2 --stop SUBMIT_VAT_RETURN_FRONTEND`
3. Run the frontend locally using `./run.sh`


## Test the application

### Unit and Integration tests
To run the unit and integration tests, you can either use ```sbt test it:test``` or ```sbt clean coverage test it:test coverageReport``` 
or ```./run-all-tests.sh```

### Useful VRN for local/staging testing

| VRN  | Result |
|:-----|:------:|
| 444444444 |  Successful Submission  |
| 000000002 |  Submission Error  |
| 444444552 |  Insolvent User  |

#### Note:
To test the Unauthorised error page for agents -> Set the 'Affinity group' to “Agent” without an HMRC-AS-AGENT enrolment

To test the Unauthorised error page for non agents -> Set the 'Affinity group' to “Individual” or “Organisation” without any enrolment


### License
This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
