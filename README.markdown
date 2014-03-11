# Lloyd's Spray-servlet scratchpad

Exploring Spray as a REST API framework.

Things to look at:

1. Integration with Spray-Client for non-blocking calls to external services
2. Metrics measurement

## Swagger

Swagger documentation can be seen at `/swagger/`

## On project structure

Making Swagger work with Spray using spray-swagger was an important part of this
project. To that end, annotations need to work and in order for annotations to work,
constraint needs to be applied when using the Spray DSL for route construction.

Towards that end, I've structured this project to more or less mimick Dropwizard, with
Resources that implement the actions that belong within a path.