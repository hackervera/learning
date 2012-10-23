(defproject learning "1.0.0-SNAPSHOT"
  :description "FIXME: write description"
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [ring/ring-core "1.1.3"]
                 [ring/ring-jetty-adapter "1.1.3"]
                 [http.async.client "0.4.5"]
                 [org.clojars.the-kenny/clojure-couchdb "0.2.2"]
                 [org.clojars.adamwynne/clj-oauth "1.2.18"]
                 [cheshire "4.0.2"]]
  :uberjar-exclusions [#"META-INF/ECLIPSEF.SF"]
  :main learning.core)
