# _intentionally_ no shebang line here

import os, sys
from urllib import request
assert sys.version_info[0] >= 3

def get_config(namespace='*', key=None): # default value for "namespace": only match the key if it`s set for _all_ namespaces within the relevant maturity level; giving "key" a default value b/c otherwise [given that "namespace" has a default value] "key" would have to come _before_ "namespace"

  # same interface as the Bash client

  # consciously allowing the following to crash if the env. var.s are not in the env.
  CONFIG_SERVER_URL     = os.environ["CONFIG_SERVER_URL"]
  CONFIG_MATURITY_LEVEL = os.environ["CONFIG_MATURITY_LEVEL"]
  

