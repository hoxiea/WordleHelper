(defproject wordle-helper "0.1.0-SNAPSHOT"
  :description "A command-line helper for your daily Wordle habit!"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}
  :plugins [[lein-auto "0.1.3"]
            [lein-count "1.0.9"]]
  :dependencies [[clojure-term-colors "0.1.0"]
                 [org.clojure/clojure "1.10.3"]]
  :main ^:skip-aot wordle-helper.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot      :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
