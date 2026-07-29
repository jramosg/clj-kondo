# False syntax findings for literal `:or` keys after `&`

**version**

`2026.07.24` or current `master`

**macro usage**

No macros.

**platform**

JVM, reproduced with the project command line.

**editor**

Not applicable.

**problem**

Clojure 1.13 map destructuring treats entries after `&` in a `:keys`
vector as literal keys. clj-kondo reports those same keys as syntax errors
when they appear in `:or`.

**repro**

Save this as `repro.clj`:

```clojure
(ns repro)

(let [{:keys [& :rebilling :repeat]
       referralamount :amount
       :or {referralamount 0
            :rebilling false
            :repeat false}}
      {:amount 13}]
  [referralamount])
```

Clojure 1.13.0-alpha6 accepts this form:

```shell
clojure -Sdeps '{:deps {org.clojure/clojure {:mvn/version "1.13.0-alpha6"}}}' \
  -M -e '(let [{:keys [& :rebilling :repeat] referralamount :amount
                :or {referralamount 0 :rebilling false :repeat false}}
               {:amount 13}]
           [referralamount])'
```

It prints:

```text
[13]
```

The entries after `&` document literal keys. They do not bind the locals
`rebilling` or `repeat`, so adding those names to the body produces the
expected Clojure unresolved-symbol error.

Run:

```shell
clojure -M:dev --lint repro.clj
```

Current output:

```text
repro.clj:6:13: error: Keys in :or should be simple symbols.
repro.clj:7:14: error: Keys in :or should be simple symbols.
```

**config**

No configuration is required.

**expected behavior**

clj-kondo should accept `:rebilling` and `:repeat` as literal keys in the
`:or` map. It should report no findings for this file.

The original report also used `:all`, but that directive covers a separate
feature.
