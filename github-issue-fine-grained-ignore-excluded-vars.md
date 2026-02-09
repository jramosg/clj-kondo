- [x] I have read the [Clojure etiquette](https://clojure.org/community/etiquette) and will respect it when communicating on this platform.

**version**

clj-kondo v2026.01.19

**platform**

JVM (also reproducible with native)

**editor**

N/A (reproduced from CLI)

**problem**

Linter-specific `#_{:clj-kondo/ignore [:specific-linter]}` on vars inside `:refer-clojure :exclude` suppresses **all** linters instead of only the listed ones. The `:unused-excluded-var` and `:unresolved-excluded-var` linters are incorrectly silenced even when they are not in the ignore list.

**repro**

```bash
❯ echo '(ns foo (:refer-clojure :exclude [comp]))' | clj-kondo --lint -
<stdin>:1:35: info: Unused excluded var: comp
linting took 36ms, errors: 0, warnings: 0

❯ echo '(ns foo (:refer-clojure :exclude [#_:clj-kondo/ignore comp]))' | clj-kondo --lint -
linting took 6ms, errors: 0, warnings: 0

❯ echo '(ns foo (:refer-clojure :exclude [#_{:clj-kondo/ignore [:invalid-arity]} comp]))' | clj-kondo --lint -
linting took 10ms, errors: 0, warnings: 0
```

The third command should still warn about `comp` being an unused excluded var — the ignore only lists `:invalid-arity`, not `:unused-excluded-var`.

**expected behavior**

`#_{:clj-kondo/ignore [:invalid-arity]}` should only suppress `:invalid-arity` findings. Unrelated linters like `:unused-excluded-var` and `:unresolved-excluded-var` should still fire.

The root cause is `utils/ignored?` which checks if `:clj-kondo/ignore` metadata exists but does not check whether the specified linter types match. The proper type-aware check in `findings/ignored?` → `ignore-match?` is never reached because `utils/ignored?` short-circuits first.

I'm interested in doing a PR for this.
