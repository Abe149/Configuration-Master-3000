package org.example.IPv4_client_authorization;

import org.example.shared.*;

import java.net.InetAddress;
import java.util.Set;
import java.util.HashSet;
import java.io.*;


// private class engine_state // I realized after typing this: why bother? // DE

public class IPv4_client_authorization_engine {

  // strategic plan: the language accepts [Java] regexes, and when these regexes are processed there is an implicit leading '^' and an implicit trailing '$'
  private Set<String>  blacklisted_FQDN_patterns = new HashSet<String>();
  private Set<String>  whitelisted_FQDN_patterns = new HashSet<String>();

  // strategic plan: short[4], use values >=0 for literal numbers, -1 for '*'
  private Set<short[]>   blacklisted_IP_patterns = new HashSet<short[]>();
  private Set<short[]>   whitelisted_IP_patterns = new HashSet<short[]>();

  private strategy_types the_active_strategy_type = null; // intentionally initializing to an invalid "value"

  // strategic plan: when strictness=0, ignore duplicate directives; when strictness=1, warn about them; when strictness>1, reject inputs with duplicate directives
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
