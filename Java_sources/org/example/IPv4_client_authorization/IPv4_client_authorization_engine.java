package org.example.IPv4_client_authorization;

import org.example.shared.*;

import java.net.InetAddress;
import java.util.Set;
import java.util.HashSet;
import java.io.*;


// private class engine_state // I realized after typing this: why bother? // DE

public class IPv4_client_authorization_engine {

  private Set<String>      blacklisted_FQDNs = new HashSet<String>();
  private Set<String>      whitelisted_FQDNs = new HashSet<String>();

  // should I used something "lighter" instead, e.g. "byte[4]"?
  private Set<InetAddress> blacklisted_IPs   = new HashSet<InetAddress>();
  private Set<InetAddress> whitelisted_IPs   = new HashSet<InetAddress>();

  private strategy_types the_active_strategy_type = null; // intentionally initializing to an invalid "value"

  private boolean require_siteLocal = false;
  private boolean require_linkLocal = false;
  private boolean require_loopback  = false;

  // start of ctor
  public IPv4_client_authorization_engine(debugFriendly_buffered_input input, short strictness_level___in) throws IOException {
    final short strictness_level = strictness_level___in; // doing it "the idiom way" on purpose in case I will later need to move this [i.e. the non-"_in" version] to being a class data member variable thingy




  }




  public boolean is_connection_from_this_address_authorized(InetAddress addr) {
    if (require_siteLocal && ! addr.isSiteLocalAddress())  return false;
    if (require_linkLocal && ! addr.isLinkLocalAddress())  return false;
    if (require_loopback  && ! addr. isLoopbackAddress())  return false;

    // WIP WIP WIP //
    // WIP WIP WIP //
    // WIP WIP WIP //

    return false; // WIP WIP WIP //
  }

} // end of class "IPv4_client_authorization_engine"
