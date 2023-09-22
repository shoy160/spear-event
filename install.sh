gradle clean -Pdev=true -Pjdk=1.8 dist tar zip

# package jar
gradle clean -Pdev=true -Pjdk=1.8 jar