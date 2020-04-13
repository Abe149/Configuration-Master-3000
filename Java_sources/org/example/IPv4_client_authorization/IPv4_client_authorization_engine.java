package org.example.IPv4_client_authorization;

import java.net.InetAddress;
import java.util.Set;
import java.util.HashSet;
import org.example.shared.*;


// private class engine_state // I realized after typing this: why bother?

public class IPv4_client_authorization_engine {

  private Set<String>      blacklisted_FQDNs = new HashSet<String>();
  private Set<String>      whitelisted_FQDNs = new HashSet<String>();

  // should I used something "lighter" instead, e.g. "byte[4]"?
  private Set<InetAddress> blacklisted_IPs   = new HashSet<InetAddress>();
  private Set<InetAddress> whitelisted_IPs   = new HashSet<InetAddress>();

  private strategy_types the_active_strategy_type = null; // intentionally initializing to an invalid "value"




// debugFriendly_buffered_input

  // start of ctor
  IPv4_client_authorization_engine(debugFriendly_buffered_input input) {




  }




  boolean is_connection_from_this_address_authorized(InetAddress addr) {
    return false; // WIP WIP WIP //
  }

} // end of class "IPv4_client_authorization_engine"
