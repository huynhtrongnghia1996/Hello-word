# EFMS Automation Framework

Automation framework for EFMS UAT using Java 17, Maven, TestNG, Playwright Java, Rest Assured, PostgreSQL, Allure, Log4j2, Git and Jenkins.

## Default environment

- Environment: `UAT`
- eFMS URL: `https://uat-efms.logtechub.com/en/#/home`
- eTMS URL: `https://staging-itllog-etms.logtechub.com/en/#/app/default/home`
- Shared login username: `henry.hieu`
- Supported browsers: `chrome`, `edge`
- Default browser mode: `browser.headless=false` so Chrome/Edge opens in headed mode when a display is available

## Project structure

```text
src/main/java/com/logtechub/automation
├── api
├── config
├── database
├── logging
├── pages
│   ├── BasePage.java
│   ├── efms/EfmsHomePage.java
│   ├── etms/EtmsHomePage.java
│   └── PageManager.java
├── playwright
└── reporting

src/test/java/com/logtechub/automation
├── base
├── listeners
└── tests/ui
    ├── efms
    └── etms

src/test/resources
├── config/uat.properties
├── testng/testng-uat-ui.xml
├── testng/testng-uat-login.xml
├── allure.properties
└── log4j2.xml
```

## Local setup

Required tools:

- Java 17+
- Maven 3.9+
- Google Chrome and Microsoft Edge installed for Playwright channel mode

Install Playwright browser channels if needed:

```bash
mvn -B -DskipTests exec:java -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install --with-deps --force chrome msedge"
```

Run UAT UI suite on both Chrome and Edge:

```bash
mvn clean test -Denv=UAT -DsuiteXmlFile=src/test/resources/testng/testng-uat-ui.xml -Dbrowser.headless=false
```

Run only Chrome:

```bash
mvn clean test -Denv=UAT -DsuiteXmlFile=src/test/resources/testng/testng-uat-single-browser.xml -Dbrowser=chrome -Dbrowser.headless=false
```

Run only Edge:

```bash
mvn clean test -Denv=UAT -DsuiteXmlFile=src/test/resources/testng/testng-uat-single-browser.xml -Dbrowser=edge -Dbrowser.headless=false
```

Run in headless mode for CI/server environments:

```bash
mvn clean test -Denv=UAT -DsuiteXmlFile=src/test/resources/testng/testng-uat-ui.xml -Dbrowser.headless=true
```

Run login suite for both eFMS and eTMS.

Pass the account password at runtime through a system property:

```bash
mvn clean test -Denv=UAT -DsuiteXmlFile=src/test/resources/testng/testng-uat-login.xml -Daccount.password='<password>' -Dbrowser.headless=true
```

or through an environment variable:

```bash
export ACCOUNT_PASSWORD='<password>'
mvn clean test -Denv=UAT -DsuiteXmlFile=src/test/resources/testng/testng-uat-login.xml -Dbrowser.headless=true
```

Run tests and generate a static Allure HTML report:

```bash
mvn clean test allure:report -Denv=UAT -DsuiteXmlFile=src/test/resources/testng/testng-uat-ui.xml -Dbrowser.headless=true
```

The generated HTML entry point is:

```text
target/site/allure-maven-plugin/index.html
```

If the tests already ran and `target/allure-results` exists, generate only the HTML report:

```bash
mvn allure:report
```

## Configuration

UAT configuration is stored in `src/test/resources/config/uat.properties`.

Sensitive values can be overridden by system properties or environment variables. Examples:

```bash
mvn clean test -Ddb.url=jdbc:postgresql://host:5432/db -Ddb.username=user -Ddb.password=secret
```

or

```bash
export DB_URL=jdbc:postgresql://host:5432/db
export DB_USERNAME=user
export DB_PASSWORD=secret
```

Login password should be supplied as `-Daccount.password=...` or `ACCOUNT_PASSWORD`.

## Jenkins

`Jenkinsfile` provides a parameterized pipeline for UAT execution. Configure Jenkins tools named `java-17` and `maven-3`, plus the Allure plugin.
