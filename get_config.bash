#!/usr/bin/env bash

if [ "$0" = sh -o "$0" = /bin/sh -o "$0" = /usr/bin/sh -o "$0" = bash -o "$0" = -bash -o "$0" = /bin/bash -o "$0" = /usr/bin/bash ]; then
  echo 'Please do _not_ source this file!'
  return 1
fi

if ! which curl >/dev/null; then
  if [ -w /dev/stderr ]; then
    echo -e '\033[31mFATAL: cURL program ["curl" command] not found.\033[0m' > /dev/stderr
  fi
  exit 1
fi

readonly API_LEVEL_PREFIX=/API_version_1

# example value: 'https://localhost:4430/' [w/o the quotes, of course]
# example Bash snippet: export CONFIG_SERVER_URL='https://localhost:4430/'

if [ -z "$CONFIG_SERVER_URL" ]; then # should I default this to something "localhost"-based, for testing?  hmm...
  if [ -w /dev/stderr ]; then
    echo -e '\033[31mFATAL: required environment variable "CONFIG_SERVER_URL" not set or set to empty.\033[0m' > /dev/stderr
  fi
  exit 2
fi
CONFIG_SERVER_URL=`echo $CONFIG_SERVER_URL | sed 's,/*$,,'` # we need to string trailing slashes from the URL so we can ensure _exactly_ one slash between the end of the "authority" [i.e. either hosthame or IP by itself or hosthame or IP followed immediately by ":<port number>" and the start of "get"

config_maturity_level_pathname=/etc/Configuration_Master_3000/maturity_level # DRY

config_maturity_level=
if [[ -n "$CONFIG_MATURITY_LEVEL" && $CONFIG_MATURITY_LEVEL -ge 0 ]]; then # non-integers are >= 0 "by default"
  config_maturity_level=`echo $CONFIG_MATURITY_LEVEL | sed 's/ //g'`
  #       TO DO: output a WARNING to stderr when the ML in the env. var. seems to be invalid, i.e. a negative integer
  # maybe TO DO: output an INFO   to stderr when the ML in the env. var. seems to be OK and is going to be used 
elif [ -r $config_maturity_level_pathname ]; then
  #       TO DO: output a WARNING to stderr when the file exists but is not readable by the current effective user
  config_maturity_level=`head -n1 $config_maturity_level_pathname | sed -e 's/[#⍝].*//' -e 's/ //g'` # possible GNUisms, I dunno
  #       TO DO: output a WARNING to stderr when the ML in the file seems to be invalid, i.e. a negative integer
  # maybe TO DO: output an INFO   to stderr when the ML in the file seems to be OK and is going to be used 
  if [[ -z "$config_maturity_level" || $config_maturity_level -lt 0 ]]; then # bad [empty or negative] input from the file
    config_maturity_level=0 # HARD-CODED fallback default value of 0 [zero] [i.e. a developer`s workstation/VM]
  fi
fi

# thorough URL encoding [_too_ thorough?  encodes normal ASCII letters and digits] thanks to the replies at <https://askubuntu.com/questions/53770/how-can-i-encode-and-decode-percent-encoded-strings-on-the-command-line>

namespace="$1"
if [ -z "$namespace" ]; then
  if [ -w /dev/stderr ]; then
    echo -e '\033[31mFATAL: required first CLI arg. [namespace] not set or set to empty.\033[0m' > /dev/stderr
  fi
  exit 4
fi
namespace=`echo -n $namespace | xxd -p | tr -d '\n' | sed 's/../%&/g'`

key="$2"
if [ -z "$key" ]; then
  if [ -w /dev/stderr ]; then
    echo -e '\033[31mFATAL: required second CLI arg. [key] not set or set to empty.\033[0m' > /dev/stderr
  fi
  exit 5
fi
key=`echo $key | sed 's/ /%20/g'` # Q-and-D URL encoding

# the "-k" flag in the cURL invocation is to work around HTTPS servers with broken certificates; remove the 'k' if you want strictness
curl -fk "$CONFIG_SERVER_URL$API_LEVEL_PREFIX/get:maturity_level=$config_maturity_level,namespace=$namespace,key=$key" -w '\n'

# the exit code from this script should be 22 when the server reports a 404 and 0 when the server gives us a 200 [and, we hope, some _data_ too ;-)]

# for further reference on cURL exit codes: https://ec.haxx.se/usingcurl/usingcurl-returns
