(defproject coffeecheck "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [compojure "1.5.1"]
                 [ring/ring-defaults "0.2.1"]
                 [environ "1.1.0"]
                 [ring/ring-mock "0.3.0"]
                 [clj-http "3.7.0"]
                 [org.clojure/data.json "0.2.6"]
                 [clj-time "0.14.2"]
                 [javax.servlet/servlet-api "2.5"]
                 [com.velisco/clj-ftp "0.3.9"]
                 [ring/ring-jetty-adapter "1.3.2"]]
  :plugins [[lein-ring "0.9.7"]]
  :main coffeecheck.handler
  :ring {:handler coffeecheck.handler/app})

