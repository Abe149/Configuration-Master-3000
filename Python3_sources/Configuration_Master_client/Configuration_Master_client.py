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
    try:
      config_server_URL = open(config_server_URL_pathname, 'r').readline().split('#', 2)[0].split('⍝', 2)[0].strip()
    except:
      pass

  if not config_server_URL:
    config_server_URL = "https://localhost:4430/" # HARD-CODED fallback default value that should work well on a developer`s workstation/VM

  # the next line: not doing a "return" of this value right away, so that it will be easier for me to add more debug output later
  config_server_URL = config_server_URL.rstrip('/') # we need to string trailing slashes from the URL so we can ensure _exactly_ one slash between the end of the "authority" [i.e. either hosthame or IP by itself or hosthame or IP followed immediately by ":<port number>" and the start of "get"
  return config_server_URL


def can_convert_to_int(arg):
  if not arg:  return False
  try:
    _ = int(arg)
    return True
  except ValueError:
    return False

def get_ML(): # can return either an int or an str
  config_ML_env_var_name = "CONFIG_MATURITY_LEVEL" # DRY
  config_ML = ""
  if config_ML_env_var_name in os.environ:
    config_ML = os.environ[config_ML_env_var_name].strip()

  if config_ML and ((not can_convert_to_int(config_ML)) or (int(config_ML) >= 0)):
    return config_ML

  config_ML_pathname="/etc/Configuration_Master_3000/maturity_level" # for clarity, i.e. for readability of code
  try:
    config_ML = open(config_ML_pathname, 'r').readline().split('#', 2)[0].split('⍝', 2)[0].strip()
  except:
    pass

  if config_ML and ((not can_convert_to_int(config_ML)) or (int(config_ML) >= 0)):
    return config_ML
    
  return 0 # HARD-CODED fallback default value that should work well on a developer`s workstation/VM


def get_config_as_Boolean(namespace='*', key=None, ML=str(get_ML())): # DEPRECATED ambiguous API function name; wrapping for backwards compatibility "for now" [until 2.0, non-inclusive?]
  print ("WARNING: deprecated CM3000 Python client API function name ''" + get_subroutine_name() + "'' used.")
  return   get___config_as_Boolean___or_exception(namespace=namespace, key=key, ML=ML)

def get___config_as_Boolean___or_NoneType(namespace='*', key=None, ML=str(get_ML())):
  try:
    return get___config_as_Boolean___or_exception(namespace=namespace, key=key, ML=ML)
  except:
    return None
  return None # just in case

def get___config_as_Boolean___or_exception(namespace='*', key=None, ML=str(get_ML())):
  import re
  temp = get___config_as_string___or_exception(namespace=namespace, key=key, ML=ML)
  if re.match(".*(false|no|off| 0 )"    , temp.lower(), re.IGNORECASE):  return False # no need for a trailing ".*" in the regex
  if re.match(".*(true|yes|on| 1 | -1 )", temp.lower(), re.IGNORECASE):  return  True # no need for a trailing ".*" in the regex
  raise ValueError("in Configuration Master 3000 client: SYNTAX ERROR while trying to get as a Boolean: ''" + temp + "''")


def get_config_as_integer(namespace='*', key=None, ML=str(get_ML())): # DEPRECATED ambiguous API function name; wrapping for backwards compatibility "for now" [until 2.0, non-inclusive?]
  print ("WARNING: deprecated CM3000 Python client API function name ''" + get_subroutine_name() + "'' used.")
  return   get___config_as_integer___or_exception(namespace=namespace, key=key, ML=ML)

def get___config_as_integer___or_NoneType(namespace='*', key=None, ML=str(get_ML())): # default value for "namespace": see below for explanation
  try:
    return get___config_as_integer___or_exception(namespace=namespace, key=key, ML=ML)
  except:
    return None
  return None # just in case

def get___config_as_integer___or_exception(namespace='*', key=None, ML=str(get_ML())): # default value for "namespace": see below for explanation
  # NOTE: this will propagate an exception [from int(str)] when the returned data is not parsable as an int
  #       _intentionally_ _not_ doing anything about that here; let the caller deal with it if they made a mistake
  return int(get___config_as_string___or_exception(namespace=namespace, key=key, ML=ML))



# default values for "namespace": only match the key if it`s set for _all_ namespaces within the relevant maturity level;
#                                 giving "key" a default value b/c otherwise [given that "namespace" has a default value]
#                                 "key" would have to come _before_ "namespace"



def get_config(           namespace='*', key=None, ML=str(get_ML())): # DEPRECATED ambiguous API function name; wrapping for backwards compatibility "for now" [until 2.0, non-inclusive?]
  print ("WARNING: deprecated CM3000 Python client API function name ''" + get_subroutine_name() + "'' used.")
  return get___config_as_string___or_exception(namespace=namespace, key=key, ML=ML)

def get___config_as_string___or_NoneType (namespace='*', key=None, ML=str(get_ML())):
  try:
    return get___config_as_string___or_exception(namespace=namespace, key=key, ML=ML)
  except:
    return None
  return None # just in case

def get___config_as_string___or_exception(namespace='*', key=None, ML=str(get_ML())):

  # if not namespace:  raise ValueError("in Configuration Master 3000 client: empty namespace given")
  exception_prefix = "in Configuration Master 3000 client: " # DRY
  if not namespace:  raise ValueError(exception_prefix + "empty namespace given")
  if not key      :  raise ValueError(exception_prefix + "empty key "  + "given or defaulted")

  # same interface as the Bash client

  the_request = request.Request(url = get_server_URL() + API_version_prefix + ("/get:maturity_level=%s,namespace=%s,key=%s" % (pathname2url(str(ML)), pathname2url(namespace), pathname2url(key))))

  with request.urlopen(the_request) as fileLike:
    return str(fileLike.read().decode("utf-8")) # the "str()" here is probably unnecessary, but just "making sure" [given Python]




# thanks to <https://stackoverflow.com/questions/251464/how-to-get-a-function-name-as-a-string>, more specifically <https://stackoverflow.com/questions/251464/how-to-get-a-function-name-as-a-string/36228241#36228241>

def get_subroutine_name():
  import traceback
  return traceback.extract_stack(None, 2)[0][2]





### from here on down: API functions that have not yet been disambiguated as to their error behavior [exception vs. None] ###


def get_test():
  config_server_URL = get_server_URL()

  the_request = request.Request(url = config_server_URL + API_version_prefix + "/test")

  with request.urlopen(the_request) as fileLike:
    return fileLike.read().decode("utf-8")


def get_type(namespace='*', key=None): # default value for "namespace": only match the key if it`s set for _all_ namespaces within the relevant maturity level; giving "key" a default value b/c otherwise [given that "namespace" has a default value] "key" would have to come _before_ "namespace"

  # if not namespace:  raise ValueError("in Configuration Master 3000 client: empty namespace given")
  exception_prefix = "in Configuration Master 3000 client: " # DRY
  if not namespace:  raise ValueError(exception_prefix + "empty namespace given")
  if not key      :  raise ValueError(exception_prefix + "empty key "  + "given or defaulted")

  # same interface as the Bash client

  the_request = request.Request(url = get_server_URL() + API_version_prefix + ("/get_type:namespace=%s,key=%s" % (pathname2url(namespace), pathname2url(key))))

  with request.urlopen(the_request) as fileLike:
    return fileLike.read().decode("utf-8")
