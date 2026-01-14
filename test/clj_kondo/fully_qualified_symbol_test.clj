(ns clj-kondo.fully-qualified-symbol-test
  (:require [clj-kondo.test-utils :refer [lint! assert-submaps2]]
            [clojure.test :refer [deftest is testing]]))

(def config {:linters {:fully-qualified-symbol {:level :warning}}})

(deftest fully-qualified-symbol-with-required-namespace-test
  (testing "fully qualified symbol with required namespace"
    (assert-submaps2
     [{:file "<stdin>",
       :row 1,
       :col 39,
       :level :warning,
       :message "Use alias for clojure.string"}]
     (lint! "(ns foo (:require [clojure.string])) (clojure.string/join [] [])"
            config))))

(deftest fully-qualified-symbol-with-aliased-namespace-test
  (testing "fully qualified symbol with aliased namespace"
    (assert-submaps2
     [{:file "<stdin>",
       :row 1,
       :col 47,
       :level :warning,
       :message "Use alias str for clojure.string"}]
     (lint! "(ns foo (:require [clojure.string :as str])) (clojure.string/join [] [])"
            config))))

(deftest aliased-usage-should-not-warn-test
  (testing "aliased usage should not warn"
    (is (empty? (filter #(= :fully-qualified-symbol (:type %))
                        (lint! "(ns foo (:require [clojure.string :as str])) (str/join [] [])"
                               config))))))

(deftest fully-qualified-symbol-in-value-position-test
  (testing "fully qualified symbol in value position"
    (assert-submaps2
     [{:file "<stdin>",
       :row 1,
       :col 49,
       :level :warning,
       :message "Use alias for clojure.set"}]
     (lint! "(ns foo (:require [clojure.set])) (map identity clojure.set/union)"
            config))))

(deftest fully-qualified-symbol-with-non-required-namespace-test
  (testing "fully qualified symbol with non-required namespace (should be ignored by this linter)"
    (is (assert-submaps2
         '({:file "<stdin>"
            :row 1
            :col 11
            :level :warning
            :message "Unresolved namespace clojure.string. Are you missing a require?"})
         (lint! "(ns foo) (clojure.string/join [] [])"
                (assoc-in config [:linters :unresolved-namespace :level] :warning))))))

(deftest clojure-core-usage-test
  (testing "clojure.core usage"
    (assert-submaps2
     [{:file "<stdin>",
       :row 1,
       :col 11,
       :level :warning,
       :message "Use alias for clojure.core"}]
     (lint! "(ns foo) (clojure.core/map identity [])"
            config))))

(deftest linter-disabled-test
  (testing "linter is info by default"
    (is (assert-submaps2
         '({:file "<stdin>"
            :row 1
            :col 39
            :level :info
            :message "Use alias for clojure.string"})
         (lint! "(ns foo (:require [clojure.string])) (clojure.string/join [])")))))

(comment
  (require '[clojure.java.io :as io]) 
  (lint! (io/file "inlined/clj_kondo/impl/toolsreader/v1v2v2/clojure/tools/reader/reader_types.clj")))