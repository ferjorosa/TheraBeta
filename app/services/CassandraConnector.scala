package services

/**
 * Created by Fer on 24/02/2015.
 */

import com.websudos.phantom.zookeeper.{DefaultZookeeperConnector, SimpleCassandraConnector}

/**
 * This is an example of how to connect to Cassandra in the easiest possible way.
 * The SimpleCassandraConnector is designed to get you up and running immediately, with almost 0 effort.
 *
 * What you have to do now is to tell phantom what keyspace you will be using in Cassandra. This connector will automaticalyl try to connect to localhost:9042.
 * If you want to tell the connector to use a different host:port combination, simply override the address inside it.
 *
 * Otherwise, simply mixing this connector in will magically inject a database session for all your queries and you can immediately run them.
 */
trait ExampleConnector extends SimpleCassandraConnector {
  val keySpace = "prueba"
}

/**
 * Now you might ask yourself how to use service discovery with phantom. The Datastax Java Driver can automatically connect to multiple clusters.
 * Using some underlying magic, phantom can also help you painlessly connect to a series of nodes in a Cassandra cluster via ZooKeeper.
 *
 * Once again, all you need to tell phantom is what your keyspace is. Phantom will make a series of assumptions about which path you are using in ZooKeeper.
 * By default, it will try to connect to localhost:2181, fetch the "/cassandra" path and parse ports found in a "host:port, host1:port1,
 * .." sequence. All these settings are trivial to override in the below connector and you can adjust all the settings to fit your environment.
 */
trait ZooKeeperConnector extends DefaultZookeeperConnector {
  val keySpace = "phantom_zookeeper_example"
}
