# CovidCertificate-API-Gateway-Service

The API gateway service provides an API for third party systems.

This project is released by the the Federal Office of Information Technology, Systems and Telecommunication FOITT on behalf of the Federal Office of Public Health FOPH.

## Content Signature
Every request to `/api/v1/covidcertificate/*` requires a valid signature of the body. The creation of this signature is documented [here](https://github.com/admin-ch/CovidCertificate-Apidoc#content-signature).

## Local use
To run the service locally without connecting to the eIAM AdminService, add the profiles "local,mock-identity-authorization" to the run configuration.

## Lombok
Project uses Lombok. Configure your IDE with lombok plugin.
