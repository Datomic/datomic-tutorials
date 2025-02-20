# Building a TODO List App with Clojure + Datomic Pro

Are you looking for a way to get started learning Datomic, or are you picking Datomic up again after some time away from it? If so, this tutorial is for you. By building a straightforward to-do list app with Clojure and Datomic, we will identify the mental model of Datomic and demonstrate how to use its theoretical insights in a practical context. Your journey through the tutorial will equip you with the skills to continue exploring Datomic on your own.

As we go along, we will primarily reference [docs.datomic.com](https://docs.datomic.com/datomic-overview.html) and list other resources for further reading and learning and the end of each part of the tutorial.

## Technology Requirements

- [Java](https://clojure.org/guides/install_clojure#java)
- [Clojure](https://clojure.org/guides/install_clojure)
- Browser

In a new terminal, verify that the requirements are installed properly by running the following commands.

```shell
clojure --version
```

```shell
;; Output
Clojure CLI version 1.12.0.1479
```

It's recommended to install the latest stable version of Clojure.

```shell
java --version
```

```shell
;; Output
java 21.0.4 2024-07-16 LTS
Java(TM) SE Runtime Environment Oracle GraalVM 21.0.4+8.1 (build 21.0.4+8-LTS-jvmci-23.1-b41)
Java HotSpot(TM) 64-Bit Server VM Oracle GraalVM 21.0.4+8.1 (build 21.0.4+8-LTS-jvmci-23.1-b41, mixed mode, sharing)
```

It should work with Java 8, 11, 17 and 21

### Before you start

This tutorial expects that you're familiar with Clojure and progamming at REPL. If you are totally new to Clojure, you should seek out some more introductory content before coming back here.  Clojure is the absolute most important requirement to use Datomic effectively, the better you get at the language the better you'll get at using Datomic.

Here is a suggestion of content:

- [Learn Clojure](https://clojure.org/guides/learn/clojure)
- [Clojure destructuring](https://clojure.org/guides/destructuring)
- [Clojure threading_macros](https://clojure.org/guides/threading_macros)
- [Clojure map](https://clojuredocs.org/clojure.core/map)
- [Clojure for](https://clojuredocs.org/clojure.core/for)
- [Programming at REPL](https://clojure.org/guides/repl/introduction)

We highly recommend to spend time to setup your [editor](https://clojure.org/guides/editors) environment to interact with a Clojure REPL.

### Glossary

If at any point some word it’s not clear or was left out from clear explanation the official glossary offers a semantically description.

[![img](https://docs.datomic.com/impl/favicon.ico)Glossary | Datomic](https://docs.datomic.com/glossary.html?search=da#datom)

## Building the App

We want to build an app that will do everything we typically expect of a todo app (create list items, mark items completed, delete items), plus a couple of interesting features. For example, have you ever wanted add some time filters so that you could ask your app, "How was my list yesterday?" In *Part 1*, we'll focus on the foundation that will allow us to add more features as we go.

### Parts

1. Install Datomic and explore it using the REPL
2. Build a simple UI and display the database data there
3. CRUD for Lists and Items
4. Add filters, time based, by status and display the history of status transitions.
5. Deploy application
6. Use [Datomic Cloud](https://www.datomic.com/cloud.html) as the database

### Technologies

We'll keep dependencies small.

- [Clojure](https://clojure.org/)
- [Datomic](https://www.datomic.com/)
- [Pedestal](http://pedestal.io/pedestal/0.7/index.html)
- [Hiccup](https://github.com/weavejester/hiccup) for HTML and CSS. No need for JavaScript!

#### Project Structure

Choose a place in your file system to create a new project, in this tutorial we’ll use the path when a new terminal is open.  We’ll follow this structure.

```
.
├── deps.edn
└── src
    └── todo_db.clj
```

We'll have a `deps.edn` to manage dependencies, a `src` folder that contains the Clojure files. To get started, we'll have one `server.clj` file that contains the code for Pedestal server and `todo_db.clj` for the Datomic interaction.

Run the commands below to create the folder structure and files.

```
mkdir -p datomic-todo-list/src touch datomic-todo-list/deps.edn datomic-todo-list/src/server.clj datomic-todo-list/src/todo_db.clj
```

Now let’s double check files where created properly

```
cd datomic-todo-list && ls . src
```

```
;; OUTPUT
.: deps.edn  src src: server.clj todo_db.clj
```

### Installation

Open a new terminal and download datomic-pro by running these commands.

```bash
curl https://datomic-pro-downloads.s3.amazonaws.com/1.0.7075/datomic-pro-1.0.7075.zip -O unzip datomic-pro-1.0.7075.zip -d .
```

it will output many lines that will looks something like this:

```bash
;; Output .......  inflating: ./datomic-pro-1.0.7075/presto-server/bin/procname/Linux-ppc64le/libprocname.so     creating: ./datomic-pro-1.0.7075/presto-server/bin/procname/Linux-x86_64/  inflating: ./datomic-pro-1.0.7075/presto-server/bin/procname/Linux-x86_64/libprocname.so     creating: ./datomic-pro-1.0.7075/presto-server/bin/procname/Linux-aarch64/  inflating: ./datomic-pro-1.0.7075/presto-server/bin/procname/Linux-aarch64/libprocname.so    inflating: ./datomic-pro-1.0.7075/presto-server/bin/launcher    inflating: ./datomic-pro-1.0.7075/presto-server/bin/launcher.py  inflating: ./datomic-pro-1.0.7075/presto-server/bin/launcher    inflating: ./datomic-pro-1.0.7075/presto-server/bin/launcher.py
```

#### Run Transactor with Default Properties

The **transactor** is a process with the ability to commit transactions for a given database. For local development Datomic’s dev storage uses an [H2](https://www.h2database.com/html/main.html) database embedded in the transactor process, with a default configuration that exposes the database on ports 4334 and 4335.

In the same terminal, run the following command to start the transactor:

```
datomic-pro-1.0.7075/bin/transactor -Ddatomic.printConnectionInfo=true config/samples/dev-transactor-template.properties
```

```
;; Output Launching with Java options -server -Xms1g -Xmx1g  -Ddatomic.printConnectionInfo=true Starting datomic:dev://localhost:4334/<DB-NAME>, storing data in: ./data ... System started datomic:dev://localhost:4334/<DB-NAME>, storing data in: ./data
```

Now that we have a running transactor we can start working on the code.

## REPL Explorations

It's good to explore the technology in front to get a better understatement of it and that will help us make better decisions when it comes to solving the problem we want. Let's do some REPL exploration and get some practice with Datomic essentials:

1. Setup a connection to Datomic using the Clojure library
2. Schema
3. Transact
4. Query

Let's make sure we have the dependencies we need in `deps.edn`.

``` clojure
{:deps {org.clojure/clojure {:mvn/version "1.12.0"}
        com.datomic/peer {:mvn/version "1.0.7260"}
        io.pedestal/pedestal.jetty {:mvn/version "0.7.1"}
        org.slf4j/slf4j-simple {:mvn/version "2.0.10"}
        hiccup/hiccup {:mvn/version "2.0.0-RC3"}}}
```

In the terminal we can run

```shell
clj
```

and a REPL will appear, that means that the dependencies are downloaded correctly

```shell
Clojure 1.12.0
user=>
```

For now, we'll focus in the `com.datomic/peer` library. The rest will come handy when building the browser UI.

Let's create a new file called `src/todo_db.clj` and connect it to a running REPL (link to instructions on how to do it).

```shell
touch src/todo_db.clj
```

Let's think about a schema that will help us get something working.

> Lists has many items

That's it, we don't need anything else. Just `List`  and `Todo` entities.

``` clojure
(def schema
  [{:db/ident       :list/name
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one
    :db/unique      :db.unique/identity
    :db/doc         "List name"}
   {:db/ident       :list/items
    :db/valueType   :db.type/ref
    :db/cardinality :db.cardinality/many
    :db/doc         "List items referenece"}
   {:db/ident       :item/status
    :db/valueType   :db.type/ref
    :db/cardinality :db.cardinality/one
    :db/doc         "Item Status"}
   {:db/ident       :item/text
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc         "Item text"}
   {:db/ident :item.status/waiting}
   {:db/ident :item.status/doing}
   {:db/ident :item.status/done}])
```

*Learn more about defining schema in Datomic [here](https://docs.datomic.com/schema/schema-reference.html#defining-schema)*

We'll do `:list/items` a [cardinality](https://docs.datomic.com/schema/schema-reference.html#db-cardinality) many attribute with a `:db.type/ref`. The Item entity will have `:item/status` as [enumeration](https://docs.datomic.com/schema/schema-modeling.html#enums) and `:item/text` that serves well for now.

The Datomic Peer API names databases with a URI that includes the protocol name, storage connection information, and a database name. The complete URI for a database named "todo" on the transactor you started in the previous step is `datomic:dev://localhost:4334/todo`.

Let's connect to the database and transact the initial schema inside `src/todo_db.clj`.

``` clojure
(ns todo-db
  (:require
   [datomic.api :as d]))

(def schema
  [{:db/ident       :list/name
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one
    :db/unique      :db.unique/identity
    :db/doc         "List name"}
   {:db/ident       :list/items
    :db/valueType   :db.type/ref
    :db/cardinality :db.cardinality/many
    :db/doc         "List items referenece"}
   {:db/ident       :item/status
    :db/valueType   :db.type/ref
    :db/cardinality :db.cardinality/one
    :db/doc         "Item Status"}
   {:db/ident       :item/text
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc         "Item text"}
   {:db/ident :item.status/waiting}
   {:db/ident :item.status/doing}
   {:db/ident :item.status/done}])

(def db-uri "datomic:dev://localhost:4334/todo")

;; INFO: Creates the database, if it does not exist returns false
(d/create-database db-uri) ;; it requires a running transactor

;; INFO: to delete a database use `d/delete-database`
(comment (d/delete-database db-uri))

;; Connect to the database
(def conn (d/connect db-uri))
```

Load the file to the REPL and then [navigate](https://clojure.org/guides/repl/navigating_namespaces) to the `todo-db` namespace in the REPL.

``` clojure
;; REPL
(in-ns 'todo-db)
@(transact conn [schema])
```

```clojure
;; Result
{:db-before datomic.db.Db@b36cf964,
 :db-after datomic.db.Db@1a19c383,
 :tx-data
 [#datom[13194139534316 50 #inst "2025-02-17T16:27:36.893-00:00" 13194139534316 true] #datom[73 62 "List items referenece" 13194139534316 true] #datom[73 62 "List items referenece" 13194139534316 false] #datom[74 62 "Item Status" 13194139534316 true] #datom[74 62 "Todo Status" 13194139534316 false] #datom[75 62 "Item text" 13194139534316 true] #datom[75 62 "Todo text" 13194139534316 false]],
 :tempids
 {-9223300668110598115 72,
  -9223300668110598114 73,
  -9223300668110598113 74,
  -9223300668110598112 75,
  -9223300668110598111 17592186045417,
  -9223300668110598110 17592186045418,
  -9223300668110598109 17592186045419}}

```

With the schema transacted we are able to store some lists and items. For that we'll create a function `new-list` that receives a *list-name* and returns a [datom](https://docs.datomic.com/glossary.html#datom) that we'll use to transact a new List.

```clojure
(defn new-list [list-name]
  [:db/add "list.id" :list/name list-name])
```

We need to pass a [tempid](https://docs.datomic.com/transactions/transaction-data-reference.html#tempids) `"list.id"` so that Datomic will treat it as a new entity. Once it's transacted it will receive it's own [entity id](https://docs.datomic.com/glossary.html#entity-id) also known as **eid**.

#### Transact a New List

``` clojure
;; REPL
@(d/transact conn [(new-list "rissotto")]) ;; d/transact receives a conn and a vector of datoms.
```

```clojure
;; Result
{:db-before datomic.db.Db@ce9d18ae,
 :db-after datomic.db.Db@b3aa3ce4,
 :tx-data
 [#datom[13194139534316 50 #inst "2025-02-17T16:29:42.229-00:00" 13194139534316 true] #datom[17592186045421 72 "rissotto" 13194139534316 true]],
 :tempids {"list.id" 17592186045421}}
```

In the result map (tx-report) there is `:tempids`. It contains the values of the given temp id ("list.id") with the given eid (17592186045421) in the database.

#### Transact New Items

With a list transacted, let's start adding some items. We'll follow the same pattern: create a function `new-item` that receives a *list-name* and the todo string. We are also including the [db](https://docs.datomic.com/glossary.html#database) as we want to get the eid of the list to make the correct relationship.

```clojure
(defn new-item [db list-name item-text]
  {:db/id (d/entid db [:list/name list-name])
   :list/items [{:db/id "item.temp"
                 :item/text item-text
                 :item/status :item.status/waiting}]})
```

In this function, we are making use of [map forms](https://docs.datomic.com/transactions/transaction-data-reference.html#map-forms) as a shorthand for set of additions.

The tempid ("todo.temp") is for the todo entity because the list is persisted in the database and we must use the eid, otherwise it will create a new list. To get the eid of an entity we can use `d/entid` function that receives a db and a [lookup ref](https://docs.datomic.com/schema/identity.html#lookup-refs).

```clojure
;; REPL
@(d/transact conn [(new-item (d/db conn) "rissotto" "cheese")]) ;; d/transact receives a conn and a vector of datoms.
```

```clojure
;; Result
{:db-before datomic.db.Db@b3aa3ce4,
 :db-after datomic.db.Db@e87d546e,
 :tx-data
 [#datom[13194139534318 50 #inst "2025-02-17T16:34:03.502-00:00" 13194139534318 true] #datom[17592186045421 73 17592186045423 13194139534318 true] #datom[17592186045423 75 "cheese" 13194139534318 true] #datom[17592186045423 74 17592186045417 13194139534318 true]],
 :tempids {"item.temp" 17592186045423}}

```

We have our initial schema working, now let's populate it with a bit more data to run some exploration queries.

```clojure
;; REPL
@(d/transact conn [(new-list "life")]) ;; create one more list: "life"
(def db (d/db conn))
(->> ["travel" "play drums" "scuba dive" "print photos"]
     (map (partial new-item db "life"))
     (d/transact conn))
```

This fails. The error below is the exception. It is telling us that we are trying to persist two datoms with the same eid.

```clojure
   {:cognitect.anomalies/category :cognitect.anomalies/incorrect,
    :cognitect.anomalies/message
    "Two datoms in the same transaction conflict\n{:d1 [17592186045428 :item/text \"travel\" 13194139534323 true],\n :d2 [17592186045428 :item/text \"play drums\" 13194139534323 true]}\n",
    :d1 [17592186045428 :item/text "travel" 13194139534323 true],
    :d2 [17592186045428 :item/text "play drums" 13194139534323 true],
    :db/error :db.error/datoms-conflict,
    :tempids {"item.temp" 17592186045428}} ;; Here is the tempid
```

Current *new-item* function

```clojure
(defn new-item [db list-name item-text]
  {:db/id (d/entid db [:list/name list-name])
   :list/items [{:db/id "item.temp" ;; IMPORTANT line
                 :item/text item-text
                 :item/status :item.status/waiting}]})
```

As we are generating many items in the same transaction that tempid needs to be different for each item, otherwise Datomic treats it as the same entity. Let's fix it.

```clojure
(defn new-item [db list-name item-text]
  (let [minify (clojurstring/replace item-text #" " "-")]
    {:db/id (d/entid db [:list/name list-name])
     :list/items [{:db/id  (str "item.temp." minify) ;; IMPORTANT line
                   :item/text item-text
                   :item/status :item.status/waiting}]}))
```

Run:

```clojure
;; REPL
(map (partial new-item db "life") ["travel" "play drums" "scuba dive" "print photos"])
```

```clojure
;; Result
({:db/id 17592186045425,
  :list/items
  [{:db/id "item.temp.travel",
    :item/text "travel",
    :item/status :item.status/waiting}]}
 {:db/id 17592186045425,
  :list/items
  [{:db/id "item.temp.play-drums",
    :item/text "play drums",
    :item/status :item.status/waiting}]}
 {:db/id 17592186045425,
  :list/items
  [{:db/id "item.temp.scuba-dive",
    :item/text "scuba dive",
    :item/status :item.status/waiting}]}
 {:db/id 17592186045425,
  :list/items
  [{:db/id "item.temp.print-photos",
    :item/text "print photos",
    :item/status :item.status/waiting}]})

```

Great! Now each `:db/id` inside `:list/todos` is different. Let's transact to the db.

```clojure
;; REPL
(def db (d/db conn))
(->> ["travel" "play drums" "scuba dive" "print photos"]
     (map (partial new-item db "life"))
     (d/transact conn))
```

```clojure
;; Result
  {:db-before datomic.db.Db@d1f79ed6,
   :db-after datomic.db.Db@6842ff52,
   :tx-data
   [#datom[13194139534323 50 #inst "2025-02-17T16:40:38.253-00:00" 13194139534323 true] #datom[17592186045425 73 17592186045428 13194139534323 true] #datom[17592186045428 75 "travel" 13194139534323 true] #datom[17592186045428 74 17592186045417 13194139534323 true] #datom[17592186045425 73 17592186045429 13194139534323 true] #datom[17592186045429 75 "play drums" 13194139534323 true] #datom[17592186045429 74 17592186045417 13194139534323 true] #datom[17592186045425 73 17592186045430 13194139534323 true] #datom[17592186045430 75 "scuba dive" 13194139534323 true] #datom[17592186045430 74 17592186045417 13194139534323 true] #datom[17592186045425 73 17592186045431 13194139534323 true] #datom[17592186045431 75 "print photos" 13194139534323 true] #datom[17592186045431 74 17592186045417 13194139534323 true]],
   :tempids
   {"todo.temp.travel" 17592186045428,
    "todo.temp.play-drums" 17592186045429,
    "todo.temp.scuba-dive" 17592186045430,
    "todo.temp.print-photos" 17592186045431}}>
```

Let's add some items to the `rissotto` list.

```clojure
;; REPL
(def db (d/db conn))
(->> ["rice" "dark beer" "onion" "mushrooms"]
     (map (partial new-item db "rissotto"))
     (d/transact conn))
```

```clojure
;; Result
  {:db-before datomic.db.Db@6842ff52,
   :db-after datomic.db.Db@701dcdf6,
   :tx-data
   [#datom[13194139534328 50 #inst "2025-02-17T16:41:38.558-00:00" 13194139534328 true] #datom[17592186045421 73 17592186045433 13194139534328 true] #datom[17592186045433 75 "rice" 13194139534328 true] #datom[17592186045433 74 17592186045417 13194139534328 true] #datom[17592186045421 73 17592186045434 13194139534328 true] #datom[17592186045434 75 "dark beer" 13194139534328 true] #datom[17592186045434 74 17592186045417 13194139534328 true] #datom[17592186045421 73 17592186045435 13194139534328 true] #datom[17592186045435 75 "onion" 13194139534328 true] #datom[17592186045435 74 17592186045417 13194139534328 true] #datom[17592186045421 73 17592186045436 13194139534328 true] #datom[17592186045436 75 "mushrooms" 13194139534328 true] #datom[17592186045436 74 17592186045417 13194139534328 true]],
   :tempids
   {"todo.temp.rice" 17592186045433,
    "todo.temp.dark-beer" 17592186045434,
    "todo.temp.onion" 17592186045435,
    "todo.temp.mushrooms" 17592186045436}}>
```

#### Explore Query

Datomic uses [Datalog](https://docs.datomic.com/whatis/supported-ops.html#datalog) as query engine. A query finds [values ](https://docs.datomic.com/glossary.html#value)in a [database ](https://docs.datomic.com/glossary.html#database) subject to the given constraints, and is specified as [edn](https://docs.datomic.com/glossary.html#edn). Queries are modeled following the same pattern of a Datom `[e a v t]`, if we understand this structure queries can become very powerful.

Currently we transacted 2 lists and a few items. Let's start with some simple queries to get things going.

- **Get lists and their names**

  ```clojure
  ;; REPL
  (d/q '[:find ?list-name
         :in $
         :where [?list :list/name ?list-name]]
    (d/db conn))
  ```

  ```clojure
  ;; Result
  #{["life"] ["rissotto"]}
  ```

- **Get lists + todos**

  ```clojure
  ;; REPL
  (d/q '[:find ?list-name ?items
         :in $
         :where [?list :list/name ?list-name]
                [?list :list/items ?items]]
    (d/db conn))
  ```

  ```clojure
  ;; Result
  #{["rissotto" 17592186045433] ["rissotto" 17592186045432] ["rissotto" 17592186045435] ["rissotto" 17592186045434] ["rissotto" 17592186045423] ["life" 17592186045430] ["life" 17592186045429] ["life" 17592186045428] ["life" 17592186045427]}
  ```

  What's with that result? A set of vectors repeating the list name? similar to Clojure, at first glance looks different and it's because it is a different way to interact with a database. Let's break it down, first thing we see is the repeating of list name e.g `["rissotto" 17592186045433]` and the same case for "*life*". Our schema is defined as `:db.cardinality/many` on the `:list/items` attribute, in other words we are allowing many items being referenced by `:list/items`, that makes the result make sense, it's telling us that the list "rissotto" has many items, each one being a reference.

- **Get lists + items different version**

  ```clojure
  ;; REPL
  (d/q '[:find ?list-name (vec ?items)
         :in $
         :where [?list :list/name ?list-name]
                [?list :list/items ?items]]
       (d/db conn))
  ```

  ```clojure
  ;; Result
  [["life" [17592186045430 17592186045429 17592186045428 17592186045427]]
   ["rissotto" [17592186045433 17592186045432 17592186045435 17592186045434 17592186045423]]]
  ```

  The difference is `(vec ?items)` which is grouping all of the items of a list. Also the items are numbers and that is because we are just pulling the reference number to the entity (eid), if we want to get the attributes of the Item we need to ask that explicitly.

  **Get lists + items using pull**

  ```clojure
  ;; REPL
  (d/q '[:find (pull ?list [:list/name {:list/items [:item/text]}])
         :in $
         :where [?list :list/name ?list-name]]
       (d/db conn))
  ```

  ```clojure
  ;; Result
  [[#:list{:name "rissotto",
           :items
           [#:item{:text "cheese"}
            #:item{:text "rice"}
            #:item{:text "dark beer"}
            #:item{:text "onion"}
            #:item{:text "mushrooms"}]}]
   [#:list{:name "life",
           :items
           [#:item{:text "travel"}
            #:item{:text "play drums"}
            #:item{:text "scuba dive"}
            #:item{:text "print photos"}]}]]

  ```

  In this query [pull](https://docs.datomic.com/query/query-pull.html) is very handy as we want to make an association of datoms related to the same entity.

#### Connecting the dots

So far we have:

- An initial schema.
- A function to create new lists.
- A function to create new items.
- Some queries to fetch the state of the database.

With some embellishment of the code, we can have this initial `src/todo_db.clj` file

```clojure
(ns todo-db
  (:require
   [datomic.api :as d]))

(def schema
  [{:db/ident       :list/name
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one
    :db/unique      :db.unique/identity
    :db/doc         "List name"}
   {:db/ident       :list/items
    :db/valueType   :db.type/ref
    :db/cardinality :db.cardinality/many
    :db/doc         "List items referenece"}
   {:db/ident       :item/status
    :db/valueType   :db.type/ref
    :db/cardinality :db.cardinality/one
    :db/doc         "Item Status"}
   {:db/ident       :item/text
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc         "Item text"}
   {:db/ident :item.status/waiting}
   {:db/ident :item.status/doing}
   {:db/ident :item.status/done}])

(def db-uri "datomic:dev://localhost:4334/todo")

;; INFO: Creates the database, if it does not exist returns false
(d/create-database db-uri) ;; it requires a running transactor

;; INFO: to delete a database use `d/delete-database`
(comment (d/delete-database db-uri))

;; Connect to the database
(def conn (d/connect db-uri))

(comment @(d/transact conn schema))

(defn new-list [list-name]
  [:db/add "list.id" :list/name list-name])

(defn new-item [db list-name item-text]
  (let [minify (clojure.string/replace item-text #" " "-")]
    {:db/id (d/entid db [:list/name list-name])
     :list/items [{:db/id  (str "item.temp." minify)
                   :item/text item-text
                   :item/status :item.status/waiting}]}))
```
## References

- [Datomic - docs](https://docs.datomic.com/datomic-overview.html)
- [Datomic - pull](https://docs.datomic.com/query/query-pull.html)
- [Datomic - defining a schema](https://docs.datomic.com/schema/schema-reference.html#defining-schema)
- [Datomic - db cardinality](https://docs.datomic.com/schema/schema-reference.html#db-cardinality)
- [Datomic - datom](https://docs.datomic.com/glossary.html#datom)
- [Datomic - tempid](https://docs.datomic.com/transactions/transaction-data-reference.html#tempids)
- [Datomic - entity-id](https://docs.datomic.com/glossary.html#entity-id)
- [Datomic - db](https://docs.datomic.com/glossary.html#database)
- [Datomic - map forms](https://docs.datomic.com/transactions/transaction-data-reference.html#map-forms)
- [Datomic - lookup ref](https://docs.datomic.com/schema/identity.html#lookup-refs).
- [Datomic - Datalog](https://docs.datomic.com/whatis/supported-ops.html#datalog)
- [edn](https://docs.datomic.com/glossary.html#edn)
- [Clojure](https://clojure.org/)
- [Clojure - navigate namespaces](https://clojure.org/guides/repl/navigating_namespaces)
- [Pedestal](http://pedestal.io/pedestal/0.7/index.html) HTTP server
- [Hiccup](https://github.com/weavejester/hiccup) for HTML and CSS