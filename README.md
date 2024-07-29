# net.curiousprogrammer/email-check

A small Clojure (JVM) library for email validation and verification.

## Usage

The public entrypoint is `net.curiousprogrammer.email`.
You use the `verify-email!` function like this: 

```
(require '[net.curiousprogrammer.email :as email]')

(email/verify! "testikabcd@mailinator.com" "from")
;; => {:valid? false, :email-domain "mailinator.com", :disposable? true, :mail-server "mail2.mailinator.com.", :recipient-error nil}
```

**WARNING**: a complete email verification (via `:check-recipient true`) was only on JDK 8.
It relies on an internal JDK class `sun.net.smtp.SmtpClient`
which was encapsulated into a module later and isn't available.
An alternative implementation may be provided later.
See `net.curiousprogrammer.smtp` namespace for more details.


## Development

### Run the project's tests (they'll fail until you edit them):

    $ clojure -T:build test

### Run the project's CI pipeline and build a JAR (this will fail until you edit the tests to pass):

    $ clojure -T:build ci

This will produce an updated `pom.xml` file with synchronized dependencies inside the `META-INF`
directory inside `target/classes` and the JAR in `target`. You can update the version (and SCM tag)
information in generated `pom.xml` by updating `build.clj`.

### Install it locally (requires the `ci` task be run first):

    $ clojure -T:build install

### Deploy it to Clojars

NOTE: make sure to update the artifact `version` in `build.clj` before running `clojure -T:build ci` above
and the deploy task below.
There's also a helper script `deploy-clojars.sh` that runs both.

This requires the `ci` task be run first
and needs `CLOJARS_USERNAME` and `CLOJARS_PASSWORD` environment variables (here supplied via 1Password CLI and the `clojars.env` file):

    $ op run --env-file="clojars.env" -- clojure -T:build deploy

The library will be deployed to net.curiousprogrammer/email-check on clojars.org by default.

See also https://github.com/clojars/clojars-web/wiki/Pushing


## License

Copyright © 2023 JurajMartinka.com

Distributed under the Eclipse Public License version 1.0.
