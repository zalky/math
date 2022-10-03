(defproject io.zalky/math "0.3.1"
  :description  "CLJ/CLJS math functions, macros & utilities"
  :url          "http://thi.ng/math"
  :license      {:name "Apache Software License 2.0"
                 :url "http://www.apache.org/licenses/LICENSE-2.0"
                 :distribution :repo}
  :scm          {:name "git"
                 :url "git@github.com:zalky/math.git"}

  :min-lein-vesion "2.4.0"

  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.clojure/clojurescript "1.11.4"]
                 [thi.ng/typedarrays "0.1.7"]]

  :profiles     {:dev {:dependencies [[criterium "0.4.6"]]
                       :plugins      [[lein-cljsbuild "1.1.8"]
                                      [com.cemerick/clojurescript.test "0.3.3"]]
                       :global-vars  {*warn-on-reflection* true}
                       :jvm-opts     ^:replace ["-Dclojure.compiler.direct-linking=true"]
                       :aliases      {"cleantest" ["do" "clean," "test," "cljsbuild" "test"]}}}

  :cljsbuild    {:builds [{:id           "simple"
                           :source-paths ["src" "test"]
                           :compiler     {:output-to "target/math-0.3.1.js"
                                          :optimizations :whitespace
                                          :pretty-print true}}]
                 :test-commands {"unit-tests" ["phantomjs" :runner "target/math-0.3.1.js"]}}

  :pom-addition [:developers
                 [:developer
                  [:name "Karsten Schmidt"]
                  [:url "https://thi.ng"]
                  [:timezone "0"]]])
