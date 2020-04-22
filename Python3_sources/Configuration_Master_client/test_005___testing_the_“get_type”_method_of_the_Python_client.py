#!/usr/bin/env python3

import Configuration_Master_client, os

def test(key):

  print ("in namespace '*' [i.e. only matches schema entries that are present in all namespaces]:\n\t " + "for key = ''" + key + "'': result = ''" + str(Configuration_Master_client.get_type(key=key)) + "''")
  print ()

print ()
test("key 2")
test("key 3")
test("key 4")
test("key 5")
test("test bad  nonempty_string")
test("test bad  nonnegative_integer")
test("test bad  positive_integer")
test("test bad URL")
test("test good URL")
test("test good nonempty_string")
test("test good nonnegative_integer")
test("test good positive_integer")
test("test key name for a Boolean")
test("test key name for a port number")
test("test key name for a positive integer with at least one conflict")
test("test key name for a positive integer with redundancy")
test("test key name for configuration namespace-asterisk conflict checker")
# test("")
