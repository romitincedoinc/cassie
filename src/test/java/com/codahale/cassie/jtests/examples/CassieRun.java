package com.codahale.cassie.jtests.examples;

import com.codahale.cassie.*;
import com.codahale.cassie.clocks.MicrosecondEpochClock;
import com.codahale.cassie.types.LexicalUUID;
import com.codahale.cassie.types.VarInt;
import com.codahale.cassie.types.AsciiString;
import com.codahale.cassie.types.FixedLong;
import com.codahale.cassie.codecs.Utf8Codec;

  // FIXME: Logula is not Java friendly
import com.codahale.logula.Logging;
import com.codahale.logula.Log;
import org.apache.log4j.Level;

public final class CassieRun {
  private final static Log log = Log.forClass(CassieRun.class);
  public static void main(String[] args) {
    // create a cluster with a single seed from which to map keyspaces
    Cluster cluster = new Cluster("localhost");

    // create a keyspace
    Keyspace keyspace = cluster.keyspace("Keyspace1")
      .retryAttempts(5)
      .readTimeoutInMS(5000)
      .minConnectionsPerHost(1)
      .maxConnectionsPerHost(10)
      .removeAfterIdleForMS(60000)
      .connect();

    // create a column family
    ColumnFamily<String, String, String> cass = keyspace.columnFamily("Standard1", Utf8Codec.get(), Utf8Codec.get(), Utf8Codec.get());

    log.info("inserting some columns", null);
    cass.insert("yay for me", new Column("name", "Coda"));
    cass.insert("yay for me", new Column("motto", "Moar lean."));

    cass.insert("yay for you", new Column("name", "Niki"));
    cass.insert("yay for you", new Column("motto", "Told ya."));

    cass.insert("yay for us", new Column("name", "Biscuit"));
    cass.insert("yay for us", new Column("motto", "Mlalm."));

    cass.insert("yay for everyone", new Column("name", "Louie"));
    cass.insert("yay for everyone", new Column("motto", "Swish!"));

    log.info("getting a column: %s", cass.getColumn("yay for me", "name"), null);
    // Some(Column(name,Coda,1271789761374109))

    log.info("getting a column that doesn't exist: %s", cass.getColumn("yay for no one", "name"), null);
    // None

    log.info("getting a column that doesn't exist #2: %s", cass.getColumn("yay for no one", "oink"), null);
    // None

    log.info("getting a set of columns: %s", cass.getColumns("yay for me", Set("name", "motto")), null);
    // Map(motto -> Column(motto,Moar lean.,1271789761389735), name -> Column(name,Coda,1271789761374109))

    log.info("getting a whole row: %s", cass.getRow("yay for me"), null);
    // Map(motto -> Column(motto,Moar lean.,1271789761389735), name -> Column(name,Coda,1271789761374109))

    log.info("getting a column from a set of keys: %s", cass.multigetColumn(Set("yay for me", "yay for you"), "name"), null);
    // Map(yay for you -> Column(name,Niki,1271789761390785), yay for me -> Column(name,Coda,1271789761374109))

    log.info("getting a set of columns from a set of keys: %s", cass.multigetColumns(Set("yay for me", "yay for you"), Set("name", "motto")), null);
    // Map(yay for you -> Map(motto -> Column(motto,Told ya.,1271789761391366), name -> Column(name,Niki,1271789761390785)), yay for me -> Map(motto -> Column(motto,Moar lean.,1271789761389735), name -> Column(name,Coda,1271789761374109)))

    // drop some UUID sauce on things
    cass.insert(LexicalUUID(), new Column("yay", "boo"));

    cass.<String, FixedLong, AsciiString>getColumnAs("key", 2);
    cass.insert("digits", new Column<VarInt, VarInt>(1, 300));

    log.info("Iterating!", null);
    for (Tuple2<String, Column<String,String>> row : cass.rowIterator(2)) {
      log.info("Found: %s", row._2(), null);
    }

    log.info("removing a column", null);
    cass.removeColumn("yay for me", "motto");

    log.info("removing a row", null);
    cass.removeRow("yay for me");

    log.info("Batching up some stuff", null);
    cass.batch()
      .removeColumn("yay for you", "name")
      .removeColumns("yay for us", Set("name", "motto"))
      .insert("yay for nobody", new Column("name", "Burt"))
      .insert("yay for nobody", new Column("motto", "'S funny."))
      .insert("bits!", new Column("motto", new FixedLong(20019L)))
      .execute();
  }
}
