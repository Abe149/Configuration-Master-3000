# _intentionally_ no shebang line here

import os, sys
from urllib         import request

# from urllib.parse import urlencode # unfortunately, "urlencode" does _not_ do what it says in its name IMO, at least not in/on Python 3.2.3

from urllib.request import pathname2url # this seems to do what IMO "urlencode" _should_ do



assert sys.version_info[0] >= 3

API_version_to_use = 1
API_version_prefix = "/API_version_" + str(API_version_to_use)

def get_server_URL():
  config_server_env_var_name = "CONFIG_SERVER_URL" # DRY
  config_server_URL = ""
  if config_server_env_var_name in os.environ:
    config_server_URL = os.environ[config_server_env_var_name].strip()

  if not config_server_URL:
    config_server_URL_pathname="/etc/Configuration_Master_3000/server_URL" # for clarity, i.e. for readability of code
    file = open(config_server_URL_pathname, 'r')
    config_server_URL = file.readline().split('#', 2)[0].split('⍝', 2)[0].strip()

  if not config_server_URL:
    config_server_URL = "https://localhost:4430/" # HARD-CODED fallback default value that should work well on a developer`s workstation/VM

  # the next line: not doing a "return" of this value right away, so that it will be easier for me to add more debug output later
  config_server_URL = config_server_URL.rstrip('/') # we need to string trailing slashes from the URL so we can ensure _exactly_ one slash between the end of the "authority" [i.e. either hosthame or IP by itself or hosthame or IP followed immediately by ":<port number>" and the start of "get"
  return config_server_URL


def get_test():
  CONFIG_SERVER_URL = os.environ["CONFIG_SERVER_URL"].rstrip('/') # we need to string trailing slashes from the URL so we can ensure _exactly_ one slash between the end of the "authority" [i.e. either hosthame or IP by itself or hosthame or IP followed immediately by ":<port number>" and the start of "get"

  the_request = request.Request(url = CONFIG_SERVER_URL + API_version_prefix + "/test")

  with request.urlopen(the_request) as fileLike:
    return fileLike.read().decode("utf-8")


def get_config_as_integer(namespace='*', key=None): # default value for "namespace": see below for explanation
  # NOTE: this will probably propagate an exception [from int(str)] when the returned data is not parsable as an int
  #       _intentionally_ _not_ doing anything about that here; let the caller deal with it if they made a mistake
  return int(get_config(namespace=namespace, key=key))

def get_config(           namespace='*', key=None): # default value for "namespace": only match the key if it`s set for _all_ namespaces within the relevant maturity level; giving "key" a default value b/c otherwise [given that "namespace" has a default value] "key" would have to come _before_ "namespace"

  # if not namespace:  raise ValueError("in Configuration Master 3000 client: empty namespace given")
  exception_prefix = "in Configuration Master 3000 client: " # DRY
  if not namespace:  raise ValueError(exception_prefix + "empty namespace given")
  if not key      :  raise ValueError(exception_prefix + "empty key "  + "given or defaulted")

  # same interface as the Bash client

  # consciously allowing the following to crash if the env. var.s are not in the env.
  CONFIG_SERVER_URL     = os.environ["CONFIG_SERVER_URL"].rstrip('/') # we need to string trailing slashes from the URL so we can ensure _exactly_ one slash between the end of the "authority" [i.e. either hosthame or IP by itself or hosthame or IP followed immediately by ":<port number>" and the start of "get"
  CONFIG_MATURITY_LEVEL = os.environ["CONFIG_MATURITY_LEVEL"]

  the_request = request.Request(url = CONFIG_SERVER_URL + API_version_prefix + ("/get:maturity_level=%s,namespace=%s,key=%s" % (pathname2url(CONFIG_MATURITY_LEVEL), pathname2url(namespace), pathname2url(key))))

  with request.urlopen(the_request) as fileLike:
    return fileLike.read().decode("utf-8")
