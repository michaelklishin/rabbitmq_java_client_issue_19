(defproject rabbit-generator "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [com.novemberain/langohr "3.0.0-rc2"]
                 [cheshire "5.3.1"]
                 [clj-time "0.8.0"]]
  :aliases {"consumer" ["run" "-m" "rabbit-generator.consumer"]
            "producer" ["run" "-m" "rabbit-generator.core"]}
  :jvm-opts ["-Xmx1g"])
