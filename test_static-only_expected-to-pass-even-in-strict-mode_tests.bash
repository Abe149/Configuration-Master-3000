#!/usr/bin/env bash

if [ "$0" = sh -o "$0" = /bin/sh -o "$0" = /usr/bin/sh -o "$0" = bash -o "$0" = -bash -o "$0" = /bin/bash -o "$0" = /usr/bin/bash ]; then
  echo 'Please do _not_ source this file!'
  return 1
fi

echo -e "\n\n=== running $0 ===\n"

dirs_to_try=`find Tests/static-only/expected_to_pass_even_in_strict_mode/* -maxdepth 0 -type d`
echo 'Subdir.s to try as tests:'
for dir in $dirs_to_try; do
  echo "  $dir"
done
echo

declare -i num_passed=0
declare -i num_failed=0

for dir in $dirs_to_try; do
  echo -n "testing ''$dir'', which "
  desc="`head -n 1 $dir/one-line_test_description 2>/dev/null`"
  if [ -z "$desc" ]; then
    echo 'is missing its description!:'
  else
    echo "has the description ''$desc'':"
  fi
  echo -n '  '

  failed= # empty means false here
  if ! ./check_non-strictly.sh       directory_from_which_to_load_data=$dir >/dev/null 2>/dev/null; then
    echo -en '\033[31mFAILED in non-strict mode\033[0m  '
    failed=true # it could be any non-empty string
  fi
  if ! ./check_EXTREMELY_strictly.sh directory_from_which_to_load_data=$dir >/dev/null 2>/dev/null; then
    echo -en '  \033[31mFAILED in strict mode\033[0m'
    failed=true # it could be any non-empty string
  fi
  if [ -z "$failed" ]; then
    echo -e '\033[32mPASSED\033[0m'
    num_passed+=1
  else
    echo
    num_failed+=1
  fi
done

echo

echo "Num. passed: $num_passed"
echo "Num. failed: $num_failed"



if [ $num_failed -lt 0 ]; then
  echo -e '\033[31mBUG IN TEST SCRIPT\033[0m'
  exit 1
fi

if [ $num_failed -gt 255 ]; then
  exit 255
fi

# if [ $num_failed -gt 0 ]; then
exit $num_failed
