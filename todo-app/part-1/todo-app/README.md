# Building a TODO List App with Clojure + Datomic Pro

Are you looking for a way to get started learning Datomic, or are you picking Datomic up again after some time away from it? If so, this tutorial is for you. By building a straightforward to-do list app with Clojure and Datomic, we will identify the mental model of Datomic and demonstrate how to use its theoretical insights in a practical context. Your journey through the tutorial will equip you with the skills to continue exploring Datomic on your own. You will learn key characteristics of what makes Datomic special.

- Information [accumulates over time](https://docs.datomic.com/whatis/data-model.html#indelible), and change is represented by accumulating the new, not by modifying or removing the old.
- Powerful Declarative Query, [datalog](https://docs.datomic.com/query/query.html) joins and rules provide SQL-level power, but with an easier pattern-based syntax.
- Flexible schema, A [true schema](https://docs.datomic.com/schema/schema.html), but one that can change with the changing reality of your business. [Modify it over time](https://docs.datomic.com/schema/schema-change.html).
- History of changes built-in, how and when changes where made. We'll use this property to list the changes of the statuses over time.
- Database as a value, to execute queries a database input is expected, this input can be a database pointing at different points in time. This enables queries to any point in time out of the box.

As we go along, we will primarily reference [docs.datomic.com](https://docs.datomic.com/datomic-overview.html) and list other resources for further reading and learning and the end of each part of the tutorial.

![img](assets/full-app.gif)

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
Clojure CLI version 1.12.0.1479 ;; the version might be different in your machine.
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

The tutorial can be followed through the REPL in the terminal, if you have  your [editor](https://clojure.org/guides/editors) environment to interact with a Clojure REPL run the REPL examples inside your editor.

We highly recommend to spend time to setup your [editor](https://clojure.org/guides/editors) environment to interact with a Clojure REPL. If you are able to do the following you should be good to go.

- Start a REPL and connect it to your editor.
- Evaluate Clojure code from a file(namespace) to the REPL.
- Navigate namespaces in the REPL.

### Glossary

If at any point some word it’s not clear or was left out from clear explanation the official glossary offers a semantically description.

[![img](https://docs.datomic.com/impl/favicon.ico) Glossary | Datomic](https://docs.datomic.com/glossary.html?search=da#datom)

## Building the App

We want to build an app that will do everything we typically expect of a todo app (create list items, mark items completed, delete items), plus a couple of powerful features that Datomic makes it trivial to enable them. For example, have you ever wanted add some time filters so that you could ask your app, "How was my life yesterday?" and display the evolution of the statuses of an item, a history log of changes. In *Part 1*, we'll focus on the foundation that will allow us to add more features as we go.

### Parts

1. Install Datomic and explore it using the REPL
2. [Build a simple UI and display the database data there](../../part-2/todo-app/README.md)
3. CRUD for Lists and Items
4. Add filters, time based, by status and display the history of status transitions
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

We'll have a `deps.edn` to manage dependencies, a `src` folder that contains the Clojure files. To get started, we'll have one `server.clj` file that contains the code for Pedestal server and `todo_db.clj` for the Datomic interaction. In part 1 we are going to create only `todo_db.clj`.

Choose a place in your file system to create the project and run the commands below to create the folder structure and files.

```shell
mkdir -p datomic-todo-list/src && touch datomic-todo-list/deps.edn datomic-todo-list/src/todo_db.clj
```

Now let’s double check files where created properly

```shell
cd datomic-todo-list && ls . src
```

```shell
;; OUTPUT
.: deps.edn  src src: todo_db.clj
```

### Installation

Open a new terminal and download datomic-pro by running these commands.

```shell
curl https://datomic-pro-downloads.s3.amazonaws.com/1.0.7075/datomic-pro-1.0.7075.zip -O unzip datomic-pro-1.0.7075.zip -d .
```

it will output many lines that will looks something like this:

```shell
;; Output .......
inflating: ./datomic-pro-1.0.7075/presto-server/bin/procname/Linux-ppc64le/libprocname.so     creating: ./datomic-pro-1.0.7075/presto-server/bin/procname/Linux-x86_64/  inflating: ./datomic-pro-1.0.7075/presto-server/bin/procname/Linux-x86_64/libprocname.so     creating: ./datomic-pro-1.0.7075/presto-server/bin/procname/Linux-aarch64/  inflating: ./datomic-pro-1.0.7075/presto-server/bin/procname/Linux-aarch64/libprocname.so    inflating: ./datomic-pro-1.0.7075/presto-server/bin/launcher    inflating: ./datomic-pro-1.0.7075/presto-server/bin/launcher.py  inflating: ./datomic-pro-1.0.7075/presto-server/bin/launcher    inflating: ./datomic-pro-1.0.7075/presto-server/bin/launcher.py
```

#### Run Transactor with Default Properties

The **transactor** is a process with the ability to commit transactions for a given database. For local development Datomic’s dev storage uses an [H2](https://www.h2database.com/html/main.html) database embedded in the transactor process, with a default configuration that exposes the database on ports 4334 and 4335.

In the same terminal, run the following command to start the transactor:

```shell
datomic-pro-1.0.7075/bin/transactor -Ddatomic.printConnectionInfo=true config/samples/dev-transactor-template.properties
```

```shell
;; Output
Launching with Java options -server -Xms1g -Xmx1g  -Ddatomic.printConnectionInfo=true Starting datomic:dev://localhost:4334/<DB-NAME>, storing data in: ./data ... System started datomic:dev://localhost:4334/<DB-NAME>, storing data in: ./data
```

Now that we have a running transactor we can start working on the code.

## REPL Explorations

It's good to explore the technology in front to get a better understatement of it and that, will help us make better decisions when it comes to solving the problem we want. Let's do some REPL exploration and get some practice with Datomic essentials:

1. Setup a connection to Datomic using the Clojure library
2. Schema
3. Transact
4. Query

Let's make sure we have the dependencies we need, add the following to `deps.edn`.

```clojure
{:deps {org.clojure/clojure {:mvn/version "1.12.0"}
        com.datomic/peer {:mvn/version "1.0.7260"}
        io.pedestal/pedestal.jetty {:mvn/version "0.7.1"}
        org.slf4j/slf4j-simple {:mvn/version "2.0.10"}
        hiccup/hiccup {:mvn/version "2.0.0-RC3"}}}
```
For now, we'll focus in the `com.datomic/peer` library. The rest will come handy when building the browser UI.

In the terminal that we created the project structure run

```shell
clj
```

and a REPL will appear, that means that the dependencies are downloaded correctly

```shell
Clojure 1.12.0 ;; the version might be different in your machine.
user=>
```

Alternatively you can start the REPL in your editor, **this is the recommended way**.

#### Schema

Let's think about a schema that will help us get something working.

> Lists has many items

That's it, we don't need anything else. Just `List` and `Item` entities.

```clojure
(def schema
  [{:db/ident       :list/name
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one
    :db/unique      :db.unique/identity
    :db/doc         "List name"}
   {:db/ident       :list/items
    :db/valueType   :db.type/ref
    :db/cardinality :db.cardinality/many
    :db/doc         "List items reference"}
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

Inside `src/todo_db.clj` add this code.

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
    :db/doc         "List items reference"}
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

Load the file to the REPL and then [navigate](https://clojure.org/guides/repl/navigating_namespaces) to the `todo-db` namespace in the REPL. Once there run the following code in the REPL.

```clojure
;; REPL
(in-ns 'todo-db)
(ns todo-db
  (:require
   [datomic.api :as d]))
```
```clojure
;; Result
todo-db>
```
Now we need to load the code that we have in the file to the REPL. Every time we change some code in the file we will need to run the `use` function to load the latest changes.
> If you are using your editor REPL, you can ignore this and make sure to load the code to the REPL.

```clojure
;; REPL
(use 'todo-db :reload)
```
The result should be something like this, two important things occurred.
  1. The database is created `(d/create-database db-uri)`
  2. Connection is stablished `(def conn (d/connect db-uri))`
```clojure
;; Result
[main] INFO datomic.process-monitor - {:event :metrics/initializing, :metricsCallback clojure.core/identity, :phase :begin, :pid 40809, :tid 1}
[main] INFO datomic.process-monitor - {:event :metrics/initializing, :metricsCallback clojure.core/identity, :msec 0.408, :phase :end, :pid 40809, :tid 1}
[main] INFO datomic.process-monitor - {:metrics/started clojure.core/identity, :pid 40809, :tid 1}
[clojure-agent-send-off-pool-0] INFO datomic.domain - {:event :cache/create, :cache-bytes 4156555264, :pid 40809, :tid 34}
[clojure-agent-send-off-pool-1] INFO datomic.kv-cluster - {:event :kv-cluster/get-pod, :pod-key "pod-catalog", :phase :begin, :pid 40809, :tid 50}
[clojure-agent-send-off-pool-0] INFO datomic.process-monitor - {:AvailableMB 7880.0, :ObjectCacheCount 0, :event :metrics, :pid 40809, :tid 34}
[clojure-agent-send-off-pool-1] INFO datomic.kv-cluster - {:event :kv-cluster/get-pod, :pod-key "pod-catalog", :msec 4.12, :phase :end, :pid 40809, :tid 50}
[main] INFO datomic.peer - {:event :peer/connect-transactor, :host "localhost", :alt-host nil, :port 4334, :version "1.0.7187", :pid 40809, :tid 1}
[clojure-agent-send-off-pool-1] INFO datomic.kv-cluster - {:event :kv-cluster/get-pod, :pod-key "pod-log-tail/todo-880efba5-9b7e-4b87-957b-9f2bab2ec8ea", :phase :begin, :pid 40809, :tid 50}
[clojure-agent-send-off-pool-1] INFO datomic.kv-cluster - {:event :kv-cluster/get-pod, :pod-key "pod-log-tail/todo-880efba5-9b7e-4b87-957b-9f2bab2ec8ea", :msec 2.01, :phase :end, :pid 40809, :tid 50}
[main] INFO datomic.log - {:event :log/catchup-fulltext, :phase :begin, :pid 40809, :tid 1}
[main] INFO datomic.log - {:event :log/catchup-fulltext, :msec 1.37, :phase :end, :pid 40809, :tid 1}
[main] INFO datomic.log - {:event :log/catchup, :bytes 0, :tail-t 66, :index-t 66, :msec 5, :pid 40809, :tid 1}
[clojure-agent-send-off-pool-1] INFO datomic.kv-cluster - {:event :kv-cluster/get-pod, :pod-key "pod-log-tail/todo-880efba5-9b7e-4b87-957b-9f2bab2ec8ea", :phase :begin, :pid 40809, :tid 50}
[clojure-agent-send-off-pool-1] INFO datomic.kv-cluster - {:event :kv-cluster/get-pod, :pod-key "pod-log-tail/todo-880efba5-9b7e-4b87-957b-9f2bab2ec8ea", :msec 1.31, :phase :end, :pid 40809, :tid 50}
[main] INFO datomic.log - {:event :log/catchup-fulltext, :phase :begin, :pid 40809, :tid 1}
[main] INFO datomic.log - {:event :log/catchup-fulltext, :msec 0.0713, :phase :end, :pid 40809, :tid 1}
[main] INFO datomic.log - {:event :log/catchup, :bytes 0, :tail-t 66, :index-t 66, :msec 0, :pid 40809, :tid 1}
[main] INFO datomic.peer - {:tid 1, :db-id "todo-880efba5-9b7e-4b87-957b-9f2bab2ec8ea", :protocol :dev, :db-name "todo", :port 4334, :host "localhost", :pid 40809, :event :peer/cache-connection, :system-root "localhost:4334"}
nil
```

Transact the schema with [d/transact](https://docs.datomic.com/clojure/index.html#datomic.api/transact).

```clojure
;; REPL
@(d/transact conn schema)
```

*d/transact returns a promise, we use @ to [deref](https://clojuredocs.org/clojure.core/deref) it*

```clojure
;; Result
{:db-before datomic.db.Db@56702c6b,
  :db-after datomic.db.Db@7822258e,
  :tx-data [#datom[13194139534312 50 #inst "2025-02-25T13:44:43.892-00:00" 13194139534312 true] #datom[72 10 :list/name 13194139534312 true] #datom[72 40 23 13194139534312 true] #datom[72 41 35 13194139534312 true] #datom[72 42 38 13194139534312 true] #datom[72 62 "List name" 13194139534312 true] #datom[73 10 :list/items 13194139534312 true] #datom[73 40 20 13194139534312 true] #datom[73 41 36 13194139534312 true] #datom[73 62 "List items reference" 13194139534312 true] #datom[74 10 :item/status 13194139534312 true] #datom[74 40 20 13194139534312 true] #datom[74 41 35 13194139534312 true] #datom[74 62 "Item Status" 13194139534312 true] #datom[75 10 :item/text 13194139534312 true] #datom[75 40 23 13194139534312 true] #datom[75 41 35 13194139534312 true] #datom[75 62 "Item text" 13194139534312 true] #datom[17592186045417 10 :item.status/waiting 13194139534312 true] #datom[17592186045418 10 :item.status/doing 13194139534312 true] #datom[17592186045419 10 :item.status/done 13194139534312 true] #datom[0 13 72 13194139534312 true] #datom[0 13 73 13194139534312 true] #datom[0 13 74 13194139534312 true] #datom[0 13 75 13194139534312 true]],
  :tempids {-9223300668110598110 72,
            -9223300668110598109 73,
            -9223300668110598108 74,
            -9223300668110598107 75,
            -9223300668110598106 17592186045417,
            -9223300668110598105 17592186045418,
            -9223300668110598104 17592186045419}}
```

> Datomic supports online schema evolution, meaning you can modify schema while the system is running. No DB downtime to transact schema changes. As we just did, to add novelty to the schema is like transacting other data.

With the schema transacted we are able to store some lists and items. For that we'll create a function `new-list` that receives a *list-name* and returns a [datom](https://docs.datomic.com/glossary.html#datom) that we'll use to transact a new List.

Add the `new-list` function to `todo_db.clj` file and save.
```clojure
(defn new-list [list-name]
  [:db/add "list.id" :list/name list-name])
```

We need to pass a [tempid](https://docs.datomic.com/transactions/transaction-data-reference.html#tempids) `"list.id"` so that Datomic will treat it as a new entity. Once it's transacted it will receive it's own [entity id](https://docs.datomic.com/glossary.html#entity-id) also known as **eid**.

To have the `new-list` function available in the REPL, it's necessary to reload the REPL so that it considers the lates changes in the file. We will refer to this process as **load the file to the REPL** and to do that we run the `(use 'todo-db :reload)` in the REPL.

*You might see some datomic logs after running the function or during the REPL session, for now you can ignore them.*

```clojure
;; REPL
(use 'todo-db :reload)
```

#### Transact a New List

Now transact a new list with name "life"
```clojure
;; REPL
@(d/transact conn [(new-list "life")])
```

```clojure
;; Result
{:db-before datomic.db.Db@cf3eee35,
 :db-after datomic.db.Db@6fcdb601,
 :tx-data [#datom[13194139534316 50 #inst "2025-02-25T14:00:07.382-00:00" 13194139534316 true] #datom[17592186045421 72 "life" 13194139534316 true]],
 :tempids {"list.id" 17592186045421}}
```

Let's breakdown the result, in Datomic is called `tx-report`.
- `:db-before` = database value before the transaction
- `:db-after` = database value after the transaction
- `:tx-data` = datoms produced by the transaction
- `:tempids` = tempid resolution, from the string we chose to the actual value in the database.

The tx-report enables straight forward comparisons between before/after data is transacted, and we can use the db-before to make queries to the past.

To showcase a quick example of that, create another list `learn`, but this time we are going to save the result in a var and then access to the result values.

```clojure
;; REPL
(def tx-report-learn @(d/transact conn [(new-list "learn")])) ;; new list

(d/q '[:find ?list
       :in $
       :where [?list :list/name "learn"]]
      (:db-after tx-report-learn)) ;; IMPORTANT line
```

```clojure
;; REPL
#{[17592186045423]} ;; the result number might be different for you, it's okay.
```

```clojure
;; REPL
(d/q '[:find ?list
       :in $
       :where [?list :list/name "learn"]]
      (:db-before tx-report-learn)) ;; IMPORTANT line
```

```clojure
;; REPL
#{}
```

With the result of `(def tx-report-learn @(d/transact conn [(new-list "learn")])`, you can run the same query with `(:db-after tx-report-learn)` or `(:db-before tx-report-learn)`, the first returns the eid of the list in the database, the second is empty because at that point in time the list didn't existed. This is a simple example of the **out-of-the-box** support for making queries at different moments of time, we'll do more later in the tutorial.

*learn more about [Datomic time model](https://docs.datomic.com/whatis/data-model.html#time-model)*

#### Transact New Items

With a list transacted, let's start adding some items. We'll follow the same pattern: create a function `new-item` that receives a *list-name* and the todo string. We are also including the [db](https://docs.datomic.com/glossary.html#database) as we want to get the eid of the list to make the correct relationship.

Add `new-item` function to `todo_db.clj`
```clojure
(defn new-item [db list-name item-text]
  {:db/id (d/entid db [:list/name list-name])
   :list/items [{:db/id "item.temp"
                 :item/text item-text
                 :item/status :item.status/waiting}]})
```

In this function, we are making use of [map forms](https://docs.datomic.com/transactions/transaction-data-reference.html#map-forms) as a shorthand for set of additions.

The tempid ("item.temp") is for the item entity because the list is persisted in the database and we must use the eid, otherwise it will create a new list. To get the eid of an entity we can use `d/entid` function that receives a db and a [lookup ref](https://docs.datomic.com/schema/identity.html#lookup-refs).

Make sure to **load the file to the REPL**, using `(use 'todo-db :reload)`. Then run the following in the REPL.

```clojure
;; REPL
@(d/transact conn [(new-item (d/db conn) "life" "travel")])
```

```clojure
;; Result
{:db-before datomic.db.Db@9f63d94a,
 :db-after datomic.db.Db@901ab7c7,
 :tx-data [#datom[13194139534320 50 #inst "2025-02-26T19:08:17.539-00:00" 13194139534320 true] #datom[17592186045421 73 17592186045425 13194139534320 true] #datom[17592186045425 75 "travel" 13194139534320 true] #datom[17592186045425 74 17592186045417 13194139534320 true]],
 :tempids {"item.temp.travel" 17592186045425}}
```
let's populate it with more data to run some exploration queries.

```clojure
;; REPL
(def db (d/db conn))
(->> ["play drums" "scuba dive" "buy coffee"]
     (map (partial new-item db "life"))
     (d/transact conn))
```

It fails. The error below is the exception. Is telling us that we are trying to persist two datoms with the same eid.

```clojure
   {:cause ":db.error/datoms-conflict Two datoms in the same transaction conflict\n{:d1 [17592186045427 :item/text \"play drums\" 13194139534322 true],\n :d2 [17592186045427 :item/text \"scuba dive\" 13194139534322 true]}\n"
 :data {:cognitect.anomalies/category :cognitect.anomalies/incorrect, :cognitect.anomalies/message "Two datoms in the same transaction conflict\n{:d1 [17592186045427 :item/text \"play drums\" 13194139534322 true],\n :d2 [17592186045427 :item/text \"scuba dive\" 13194139534322 true]}\n",
 :d1 [17592186045427 :item/text "play drums" 13194139534322 true],
 :d2 [17592186045427 :item/text "scuba dive" 13194139534322 true],
 :db/error :db.error/datoms-conflict,
 :tempids {"item.temp" 17592186045427}}} ;; Here is the tempid
```

Current *new-item* function

```clojure
(defn new-item [db list-name item-text]
  {:db/id (d/entid db [:list/name list-name])
   :list/items [{:db/id "item.temp" ;; IMPORTANT line
                 :item/text item-text
                 :item/status :item.status/waiting}]})
```

We are generating many items in the same transaction and the tempid needs to be different for each item, otherwise Datomic resolves to the same entity. Let's fix it.

Change the function in `todo-db.clj`
```clojure
(defn new-item [db list-name item-text]
  (let [minify (clojure.string/replace item-text #" " "-")]
    {:db/id (d/entid db [:list/name list-name])
     :list/items [{:db/id  (str "item.temp." minify) ;; IMPORTANT line
                   :item/text item-text
                   :item/status :item.status/waiting}]}))
```

Load the file to the REPL and then run

```clojure
;; REPL
(map (partial new-item db "life") ["play drums" "scuba dive" "buy coffee"])
```

```clojure
;; Result
({:db/id 17592186045421,
  :list/items [{:db/id "item.temp.play-drums",
                :item/text "play drums",
                :item/status :item.status/waiting}]}
  {:db/id 17592186045421,
   :list/items [{:db/id "item.temp.scuba-dive",
                 :item/text "scuba dive",
                 :item/status :item.status/waiting}]}
  {:db/id 17592186045421,
   :list/items [{:db/id "item.temp.buy-coffee",
                 :item/text "buy coffee",
                 :item/status :item.status/waiting}]})

```

Great! Now each `:db/id` inside `:list/items` is different. Let's transact.

```clojure
;; REPL
(def db (d/db conn))
(->> ["play drums" "scuba dive" "buy coffee"]
     (map (partial new-item db "life"))
     (d/transact conn))
```

```clojure
;; Result
{:db-before datomic.db.Db@34181b49,
 :db-after datomic.db.Db@b2a5c856,
 :tx-data [#datom[13194139534322 50 #inst "2025-02-26T19:23:24.915-00:00" 13194139534322 true] #datom[17592186045421 73 17592186045427 13194139534322 true] #datom[17592186045427 75 "play drums" 13194139534322 true] #datom[17592186045427 74 17592186045417 13194139534322 true] #datom[17592186045421 73 17592186045428 13194139534322 true] #datom[17592186045428 75 "scuba dive" 13194139534322 true] #datom[17592186045428 74 17592186045417 13194139534322 true] #datom[17592186045421 73 17592186045429 13194139534322 true] #datom[17592186045429 75 "buy coffee" 13194139534322 true] #datom[17592186045429 74 17592186045417 13194139534322 true]],
 :tempids {"item.temp.play-drums" 17592186045427,
           "item.temp.scuba-dive" 17592186045428,
           "item.temp.buy-coffee" 17592186045429}}
```

Add items to the `learn` list.

```clojure
;; REPL
(def db (d/db conn))
(->> ["clojure" "datomic" "sailing" "cook rissotto"]
     (map (partial new-item db "learn"))
     (d/transact conn))
```

```clojure
;; Result
{:db-before datomic.db.Db@e35bb6ca,
 :db-after datomic.db.Db@63981e9c,
 :tx-data [#datom[13194139534326 50 #inst "2025-02-26T19:14:50.051-00:00" 13194139534326 true] #datom[17592186045423 73 17592186045431 13194139534326 true] #datom[17592186045431 75 "clojure" 13194139534326 true] #datom[17592186045431 74 17592186045417 13194139534326 true] #datom[17592186045423 73 17592186045432 13194139534326 true] #datom[17592186045432 75 "datomic" 13194139534326 true] #datom[17592186045432 74 17592186045417 13194139534326 true] #datom[17592186045423 73 17592186045433 13194139534326 true] #datom[17592186045433 75 "sailing" 13194139534326 true] #datom[17592186045433 74 17592186045417 13194139534326 true] #datom[17592186045423 73 17592186045434 13194139534326 true] #datom[17592186045434 75 "cook rissotto" 13194139534326 true] #datom[17592186045434 74 17592186045417 13194139534326 true]],
 :tempids {"item.temp.clojure" 17592186045431,
           "item.temp.datomic" 17592186045432,
           "item.temp.sailing" 17592186045433,
           "item.temp.cook-rissotto" 17592186045434}}
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
  #{["life"] ["learn"]}
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
  #{["life" 17592186045425] ["life" 17592186045429] ["life" 17592186045428] ["life" 17592186045427] ["learn" 17592186045431] ["learn" 17592186045432] ["learn" 17592186045433] ["learn" 17592186045434]}
  ```

  What's with that result? A set of vectors repeating the list name? similar to Clojure, at first glance looks different and it's because it is a different way to interact with a database. Let's break it down, first thing we see is the repeating of list name e.g `["life" 17592186045425]` and the same case for "*life*". Our schema is defined as `:db.cardinality/many` on the `:list/items` attribute, in other words we are allowing many items being referenced by `:list/items`, that makes the result make sense, it's telling us that the list "life" has many items, each one being a reference.

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
  [["learn" [17592186045431 17592186045432 17592186045433 17592186045434]
   ["life" [17592186045425 17592186045429 17592186045428 17592186045427]]]
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
  [[#:list{:name "life",
           :items [#:item{:text "travel"}
                   #:item{:text "play drums"}
                   #:item{:text "scuba dive"}
                   #:item{:text "buy coffee"}]}]
      [#:list{:name "learn",
              :items [#:item{:text "clojure"}
                      #:item{:text "datomic"}
                      #:item{:text "sailing"}
                      #:item{:text "cook rissotto"}]}]]
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
    :db/doc         "List items reference"}
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

That's it for now, in [part 2](../../part-2/todo-app/README.md) we will do CRUD for Lists and Items. We'll create a UI and render the todo lists and items, that will be served by a Pedestal HTTP server and the HTML and CSS by Hiccup.

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