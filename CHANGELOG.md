# Change Log
All notable changes to this project will be documented in this file. This change log follows the conventions of [keepachangelog.com](http://keepachangelog.com/).

## [Unreleased]

[Unreleased]: https://github.com/net.curiousprogrammer/email-check/compare/0.1.3...HEAD


## 0.1.3 - 2024-07-29

- fix CVE in dnsjava by updating the dependency to 3.6.0
  - see https://scout.docker.com/vulnerabilities/id/CVE-2024-25638?s=github&n=dnsjava&ns=dnsjava&t=maven&vr=%3C3.6.0


## 0.1.2 - 2023-02-09

- update docstrings for the email namespace

## 0.1.1 - 2023-02-07

- more robust email domain check
- return `:format-error` describing the syntactical issues as per `validate`.

## 0.1.0 - 2023-02-07

### Added
- Basic support for email validation
- Email verification using SMTP transaction is experiment and only tested on JDK 1.8

