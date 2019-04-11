#!/usr/bin/env bash
sbt 'run 9147 -Dlogger.resource=logback-test.xml -Dapplication.router=testOnlyDoNotUseInAppConf.Routes'