(ns coffeecheck.handler
  (:require [compojure.core :refer :all]
            [clj-http.client :as http-client]
            [compojure.route :as route]
            [clojure.java.io :as io]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.adapter.jetty :as jetty]
            [miner.ftp :as ftp]
            [clojure.data.json :as json]
            [clj-time.core :as t]
            [clj-time.format :as f]
            [clj-time.local :as l]
            [environ.core :refer [env]]
            [ring.util.response :refer :all]
            [ring.middleware.json :refer [wrap-json-response]]))

(def server (env :server))
(def username (env :username))
(def password (env password))
(def ftpAddress (str "ftp://" username ":" password "@" server))
(def metadataUrl (env :metadata-url))
(def metadataFileName :metadata-file)

(defn uuid [] (str (java.util.UUID/randomUUID)))
(defn current-month [] (f/unparse (f/formatter "yyyy-MM") (l/local-now)))

(defn download [uri]
  (-> (http-client/get uri {:as :stream})
      (:body)
      (io/input-stream)))

(defn upload [ftp stream]
  (let [imageFileName (str "images/" (uuid) ".jpg")]
    (ftp/with-ftp [client ftpAddress
                   :file-type :binary]
                  (ftp/client-put-stream client stream imageFileName)
                  imageFileName)))

(defn get-meta-data [url]
  (-> (http-client/get url)
      (:body)
      (json/read-json)))

(defn init-meta-data [] [])

(defn fetch-meta-data [url]
  (try
    (get-meta-data url)
    (catch Exception e (init-meta-data))))

(defn write-to-metadata [imageFileName]
  (let [metadata (fetch-meta-data metadataUrl)]
    (conj metadata {:url imageFileName :date (current-month)})))

(defn string->stream
  ([string] (string->stream string "UTF-8"))
  ([string encoding]
   (-> string
       (.getBytes encoding)
       (java.io.ByteArrayInputStream.))))

(defn publish-metadata [metadata]
  (ftp/with-ftp [client ftpAddress
                 :file-type :binary]
                (ftp/client-put-stream client (string->stream (json/write-str metadata))
                                       metadataFileName)))

(defn process-imageUrl [imageUrl]
  (->> (download imageUrl)
       (upload ftpAddress)
       (write-to-metadata)
       (publish-metadata)))

(defroutes app-routes
           (GET "/version" [] (response {:name "checked.jirkovo.coffee" :version "1.0"}))
           (GET "/" [] (response
             {
              :server        server
              :username      username
              :ftpAddress    ftpAddress
              :metadata-url  metadataUrl
              :metadata-file metadataFileName}))
           (route/not-found "Not Found"))


(def app
  (-> app-routes
      wrap-json-response))

(defn -main []
  (let [port (Integer/parseInt (get (System/getenv) "PORT" "5000"))]
    (jetty/run-jetty app-routes {:port port})))