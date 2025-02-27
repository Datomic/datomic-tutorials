# Building a TODO List App with Clojure + Datomic Pro - [Part 2]

In [Part 1](../../part-1/todo-app/README.md), we completed the following:

- Run a Datomic database and use Clojure to interact with it
- Create and transact the schema for the project
- Transact new data
- Explore query API in the REPL

This part of the tutorial is shorter and more pragmatic than the previous one. You will build a simple HTML & CSS website that will render the List and Items that are in Datomic database.

### Building the app

#### Project structure

Create the file inside `src/server.clj`. The full project structure will look like this.
```
.
├── deps.edn
└── src
    └── server.clj
    └── todo_db.clj
```

If you come from [part 1](../part-1), in the same terminal where you created the project run the following command or create the file inside your text editor.

```shell
touch src/server.clj
```

#### Create a simple UI

The next step is to display the database's data in the browser with a simple UI. To render data in the browser, we'll use two more libraries, [Pedestal](http://pedestal.io/pedestal/0.7/index.html), for HTTP server, and [Hiccup](https://github.com/weavejester/hiccup), for HTML rendering. 

![](assets/part2.png)

Essentially we need to make a query that will provide us with the data needed to convey the image above.

First, create the HTTP server and the [Hiccup](https://github.com/weavejester/hiccup/wiki/Syntax) skeleton, then we will populate it with the results from querying Datomic.

**Pedestal routes**

Write the following functions inside `src/server.clj`

```clojure
(ns server
  (:require
   [io.pedestal.http :as http]
   [io.pedestal.http.route :as route]))

(defn hello-html
  "Receives Pedestal request map and returns a simple html text"
  [_request]
  {:status  200
   :headers {"Content-Type" "text/html; charset=utf-8"}
   :body    (str (h/html [:h1 "TODO App with Clojure + Datomic"]))})

(def routes
  (route/expand-routes
   #{["/" :get hello-html :route-name :home]}))

(defn create-server []
  (http/create-server
    {::http/routes routes
     ::http/secure-headers {:content-security-policy-settings "object-src 'none'; script-src 'self' 'unsafe-inline' 'unsafe-eval' https: http:;"}
     ::http/type :jetty
     ::http/port 8890
     ::http/join? false}))

(defonce server (atom nil))

(defn start-server []
  (reset! server (-> (create-server) http/start)))

(defn stop-server []
  (swap! server http/stop))

(defn restart-server []
  (stop-server)
  (start-server))
```

*more about [Pedestal routes](http://pedestal.io/pedestal/0.7/guides/defining-routes.html)*

Run `(start-server)` in the REPL

```clojure
;;REPL
(start-server)
```

```clojure
;; Result
#:io.pedestal.http{:port 8890, :service-fn #function[io.pedestal.http.impl.servlet-interceptor/interceptor-service-fn/fn--23980], :host "localhost", :secure-headers {:content-security-policy-settings "object-src 'none'; script-src 'self' 'unsafe-inline' 'unsafe-eval' https: http:;"}, :type :jetty, :start-fn #function[io.pedestal.http.jetty/server/fn--24550], :interceptors [#Interceptor{:name :io.pedestal.http.tracing/tracing} #Interceptor{:name :io.pedestal.http/log-request} #Interceptor{:name :io.pedestal.http/not-found} #Interceptor{:name :io.pedestal.http.ring-middlewares/content-type-interceptor} #Interceptor{:name :io.pedestal.http.route/query-params} #Interceptor{:name :io.pedestal.http.route/method-param} #Interceptor{:name :io.pedestal.http.secure-headers/secure-headers} #Interceptor{:name :io.pedestal.http.route/router} #Interceptor{:name :io.pedestal.http.route/path-params-decoder}], :routes ({:path "/", :method :get, :path-re #"/\Q\E", :path-parts [""], :interceptors [#Interceptor{}], :route-name :home, :path-params []}), :servlet #object[io.pedestal.http.servlet.FnServlet 0x56a6e7f3 "io.pedestal.http.servlet.FnServlet@56a6e7f3"], :server #object[org.eclipse.jetty.server.Server 0x71994ca0 "Server@71994ca0{STARTED}[11.0.20,sto=0]"], :join? false, :stop-fn #function[io.pedestal.http.jetty/server/fn--24552]}
```

navigate to http://localhost:8890/ in the browser, we should see something like this.

![](assets/hello-world.png)

Now build the HTML (hiccup) skeleton.

```clojure
(ns server
  (:require
   [hiccup.page :as hp] ;; new
   [hiccup2.core :as h]
   [io.pedestal.http :as http]
   [io.pedestal.http.route :as route]))

(def db-example
  [{:list/name "life"
    :list/items [{:item/text "travel"
                  :item/status {:db/ident :waiting}}
                 {:item/text "play drums"
                  :item/status {:db/ident :waiting}}
                 {:item/text "scuba dive"
                  :item/status {:db/ident :waiting}}
                 {:item/text "buy coffee"
                  :item/status {:db/ident :waiting}}]}
   {:list/name "learn"
    :list/items [{:item/text "clojure"
                  :item/status {:db/ident :waiting}}
                 {:item/text "datomic"
                  :item/status {:db/ident :waiting}}
                 {:item/text "sailing"
                  :item/status {:db/ident :waiting}}
                 {:item/text "cook rissotto"
                  :item/status {:db/ident :waiting}}]}])

(defn gen-page-head
  "Includes bootstrap css and js"
  [title]
  [:head
   [:title title]
   (hp/include-css "https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css")
   (hp/include-js "https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js")])

(defn all-lists-page
  "Renders the TODO list main page"
  [_request]
  (str
   (h/html
    (gen-page-head "TODO App with Clojure + Datomic")
    [:div {:class "container"}
     [:div {:class "bg-light rounded-3" :style "padding: 20px"}
      [:h1 {:style "color: #1cb14a"} "Lists"]]
     [:div {:class "row row-cols-2"}
      (for [list db-example
            :let [list-name (:list/name list)]]
        [:div {:class "col card"}
         [:div {:class "card-body"}
          [:h4 {:class "card-title"} list-name]
          [:table {:class "table mb-4"}
           [:thead
            [:tr
             [:th {:scope "col"} "Item"]
             [:th {:scope "col"} "Status"]]]
           [:tbody
            (for [item (:list/items list)
                  :let [item-text (get item :item/text)
                        item-status "waiting"]]
              [:tr
               [:td item-text]
               [:td
                [:span {:class "badge text-bg-light"} item-status]]])]]]])]])))
```

We define `db-example` because we are not going to query Datomic yet (patience). It is great exercise to model how you want the result to be, that will guide you towards how to create the query and even show the strengths or areas of improvement in the schema model. For those familiar with basic HTML we are creating a table. To add the values, loop over the `db-example` which is a vector of maps, each map is a "List", inside the list we have `:list/items` and then loop again to render all the items for each list.

Let's glue all things together in one file `src/server.clj`

```clojure
(ns server
  (:require
   [hiccup.page :as hp]
   [hiccup2.core :as h]
   [io.pedestal.http :as http]
   [io.pedestal.http.route :as route]))

(def db-example
  [{:list/name "life"
    :list/items [{:item/text "travel"
                  :item/status {:db/ident :waiting}}
                 {:item/text "play drums"
                  :item/status {:db/ident :waiting}}
                 {:item/text "scuba dive"
                  :item/status {:db/ident :waiting}}
                 {:item/text "buy coffee"
                  :item/status {:db/ident :waiting}}]}
   {:list/name "learn"
    :list/items [{:item/text "clojure"
                  :item/status {:db/ident :waiting}}
                 {:item/text "datomic"
                  :item/status {:db/ident :waiting}}
                 {:item/text "sailing"
                  :item/status {:db/ident :waiting}}
                 {:item/text "cook rissotto"
                  :item/status {:db/ident :waiting}}]}])

(defn gen-page-head
  "Include bootstrap css and js"
  [title]
  [:head
   [:title title]
   (hp/include-css "https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css")
   (hp/include-js "https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js")])

(defn all-lists-page
  "Renders the TODO list page"
  [_request]
  (str
   (h/html
    (gen-page-head "TODO App with Clojure + Datomic")
    [:div {:class "container"}
     [:div {:class "bg-light rounded-3" :style "padding: 20px"}
        [:h1 {:style "color: #1cb14a"} "Lists"]]
     [:div {:class "row row-cols-2"}
      (for [list db-example
            :let [list-name (:list/name list)]]
        [:div {:class "col card"}
         [:div {:class "card-body"}
          [:h4 {:class "card-title"} list-name]
          [:table {:class "table mb-4"}
           [:thead
            [:tr
             [:th {:scope "col"} "Item"]
             [:th {:scope "col"} "Status"]]]
           [:tbody
            (for [item (:list/items list)
                  :let [item-text (get item :item/text)
                        item-status "waiting"]]
              [:tr
               [:td item-text]
               [:td
                [:span {:class "badge text-bg-light"} item-status]]])]]]])]])))

(defn html-200 [body]
  {:status  200
   :headers {"Content-Type" "text/html; charset=utf-8"}
   :body    body})

(def routes
  (route/expand-routes
   #{["/" :get (comp html-200 all-lists-page) :route-name :home]}))

(defonce server (atom nil))

(defn start-server []
  (reset! server (-> (create-server) http/start)))

(defn stop-server []
  (swap! server http/stop))

(defn restart-server []
  (stop-server)
  (start-server))
```

Load the file to the REPL and then restart the server

```clojure
;; REPL
(restart-server)
```

now let's got to our browser and navigate to http://localhost:8890/ , we should see something like this.

![](assets/part2.png)

Awesome, we have our beautiful UI working, we don't need anything else for now. Next step is to fetch the data from Datomic instead of db-example. Let's go back to the `src/todo_db.clj` file and take a look at the query we use previously to fetch lists and items.

```clojure
(d/q '[:find (pull ?list [:list/name {:list/items [:item/text]}])
       :in $
       :where [?list :list/name ?list-name]]
     (d/db conn))
```

```clojure
;; =>
[[#:list{:name "life",
         :items
         [#:item{:text "travel",}
          #:item{:text "play drums",}
          #:item{:text "scuba dive",}
          #:item{:text "buy coffee",}]}]
  [#:list{:name "learn",
          :items
          [#:item{:text "clojure",}
           #:item{:text "datomic",}
           #:item{:text "sailing",}
           #:item{:text "cook rissotto",}]}]]
```

It's actually very close to what we want, instead of vectors of maps we have vector of vectors. We want to tell Datomic to bind the results into a collection, we can make use of the [binding forms](https://docs.datomic.com/query/query-data-reference.html#binding-forms), particularly the collection one `[?a ...]` , this tells Datomic to return the results as a collection of the results, it's a way to flatten the results.

```clojure
(d/q '[:find [(pull ?list [:list/name {:list/items [:item/text]}]) ...]
       :in $
       :where [?list :list/name ?list-name]]
     (d/db conn))
```
> pull is a very powerful API, it's straight forward to pull nested data without the need of joins, Datalog makes the navigation of relationships seamlessly, it has cleaner and simple semantics, codebases tend to be be more expressive and easier to understand.

```clojure
;; =>
[#:list{:name "life",
        :items
        [#:item{:text "travel",}
         #:item{:text "play drums",}
         #:item{:text "scuba dive",}
         #:item{:text "buy coffee",}]}
  #:list{:name "learn",
         :items
         [#:item{:text "clojure",}
          #:item{:text "datomic",}
          #:item{:text "sailing",}
          #:item{:text "cook rissotto",}]}]
```

The result is in the form we need, now it's matter of making the query and passing the result to the render function. Before that we create a `lists-page` function that will execute the query and place it inside `todo_db.clj`. We will also add the `:db/id` in our query result because that's the id we will use to update or retract datoms and also include the item status, `:item/status`.

Add the query to `src/todo_db.clj` save and load the file to the REPL

```clojure
(defn lists-page [db]
  (d/q '[:find [(pull ?list [:db/id :list/name {:list/items [:db/id :item/text {:item/status [:db/ident]}]}]) ...]
         :in $
         :where [?list :list/name ?list-name]]
       db))
```

*more about :db/ident [here](https://docs.datomic.com/schema/identity.html#idents)*

In the `server.clj` file we make some modifications to our `all-lists-page` render function.

```clojure
(ns server-experiment
  (:require
   [datomic.api :as d] ;; new
   [hiccup.page :as hp]
   [hiccup2.core :as h]
   [io.pedestal.http :as http]
   [io.pedestal.http.route :as route]
   [todo-db :as todo-db])) ;; new

(defn gen-page-head
  "Include bootstrap css"
  [title]
  [:head
   [:title title]
   (hp/include-css "https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css")])

(defn all-lists-page
  "Renders the TODO list page"
  [_request]
  (str
   (h/html
    (gen-page-head "TODO App with Clojure + Datomic")
    [:div {:class "container"}
     [:div {:class "bg-light rounded-3" :style "padding: 20px"}
      [:h1 {:style "color: #1cb14a"} "Lists"]]
     [:div {:class "row row-cols-2"}
      (for [list (todo-db/lists-page (d/db todo-db/conn)) ;; Important line
            :let [list-name (:list/name list)]]
        [:div {:class "col card"}
         [:div {:class "card-body"}
          [:h4 {:class "card-title"} list-name]
          [:table {:class "table mb-4"}
           [:thead
            [:tr
             [:th {:scope "col"} "Item"]
             [:th {:scope "col"} "Status"]
             [:th {:scope "col"} "Actions"]]]
           [:tbody
            (for [item (:list/items list)
                  :let [item-text (get item :item/text)
                        item-status (get-in item [:item/status :db/ident])]] ;; new
              [:tr
               [:td item-text]
               [:td
                [:span {:class "badge text-bg-light"} item-status]]
               [:td
                [:div {:class "row row-cols-auto row-cols-sm"}]]])]]]])]])))

(defn html-200 [body]
  {:status  200
   :headers {"Content-Type" "text/html; charset=utf-8"}
   :body    body})

(def routes
  (route/expand-routes
   #{["/" :get (comp html-200 all-lists-page) :route-name :home]}))

(defn create-server []
  (http/create-server
   {::http/routes routes
    ::http/secure-headers {:content-security-policy-settings "object-src 'none'; script-src 'self' 'unsafe-inline' 'unsafe-eval' https: http:;"}
    ::http/type :jetty
    ::http/port 8890
    ::http/join? false}))

(defonce server (atom nil))

(defn start-server []
  (reset! server (-> (create-server) http/start)))

(defn stop-server []
  (swap! server http/stop))

(defn restart-server []
  (stop-server)
  (start-server))
```

the `todo-db/list-page` receives a db as parameter, to get the current db in Datomic we call `d/db` which receives a connection and for this project we define the connection inside the `todo-db/conn`. With the query result we just change the value we were passing to the `for` and we should be able to get the same output.
Load the file to the REPL and call `(restart-server)`
```clojure
;;REPL
(restart-server)
```
go to http://localhost:8890/ hit reload and you should see the the items with the status too, all served by Datomic.

### Resources

- [Pedestal routes](http://pedestal.io/pedestal/0.7/guides/defining-routes.html)
- [Hiccup basic syntax](https://github.com/weavejester/hiccup/wiki/Syntax)
- [Datomic - :db/ident](https://docs.datomic.com/schema/identity.html#idents)
- [Datomic - pull](https://docs.datomic.com/query/query-pull.html)