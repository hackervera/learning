(ns learning.core
  (:require [http.async.client :as http]
            [http.async.client.request :as request]
            [cheshire.core :as cheshire]
            [ring.adapter.jetty :as jetty])
  (:use clojure.pprint)
  (:gen-class ))

(defn translate-url [url]
  (str "https://api.twitter.com/1/statuses/show.json?id=" (second (re-find #"status/(.*)" url))))

(defn translate-urls [urls]
  (map translate-url urls))

(defn get-response [url credentials]
  (with-open [client (http/create-client)]
    (let [response (http/POST client url :headers credentials)]
      (-> response
        http/await))))

(defn get-responses [urls]
  (map get-response urls))

(defn parse-urls [urls]
  (let [responses (get-responses (translate-urls urls))]
    (map cheshire/parse-string responses)))

(defn get-tweets [urls]
  (map #(str ((% "user") "screen_name") " said: " (% "text")) (parse-urls urls)))



;https://api.twitter.com/1/statuses/user_timeline.json?include_entities=true&include_rts=true&screen_name=twitterapi&count=2

(defn name-to-url [name]
  (str "https://api.twitter.com/1/statuses/user_timeline.json?include_entities=true&include_rts=true&screen_name=" name))

(defn names-to-urls [names]
  (map name-to-url names))

(defn recent-tweet [tweet]
  (map #(str ((% "user") "screen_name") " said: " (% "text")) tweet))

;tweets is a group of 5 tweets
(defn recent-messages [tweets]
  (map recent-tweet tweets))

(defn recent-tweets [names]
  (let [responses (get-responses (names-to-urls names))]
    (recent-messages (map cheshire/parse-string responses))))


(defn parse-data [request]
  (let [data (cheshire/parse-string (slurp (request :body )))
        urls (data "urlsToTweets")
        names (data "recentTweets")]
    (cond
      (not (nil? urls)) (cheshire/generate-string (get-tweets urls))
      (not (nil? names)) (cheshire/generate-string (recent-tweets names)))))


(defn request-handler [request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (parse-data request)})

(defn new-server []
  (def server (jetty/run-jetty request-handler {:port 8080 :join? false})))

(defn -main [& args]
  )


(def consumer (oauth/make-consumer
                "GCwaLlOIJaL7mFmiOBKg"
                "c28PWBko37TqXXThKc8rUQtZDzkna29mF4Xo4aF8"
                "http://twitter.com/oauth/request_token"
                "http://twitter.com/oauth/access_token"
                "http://twitter.com/oauth/authorize"
                :hmac-sha1 ))


(def request-token (oauth/request-token consumer))

(oauth/user-approval-uri consumer
  (:oauth_token request-token))


(def access-token-response (oauth/access-token consumer
                             request-token
                             "0799361"))


(def credentials (oauth/credentials consumer
                   (:oauth_token access-token-response)
                   (:oauth_token_secret access-token-response)
                   :POST
                   "http://twitter.com/statuses/update.json"
                   {:status "posting from #clojure with #oauth"}))



(http/post "http://twitter.com/statuses/update.json"
  :query (merge credentials
           {:status "testing oauth from clojure"}))
