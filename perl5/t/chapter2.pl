#!/usr/bin/perl
use strict;
use warnings FATAL => 'all';

# print(!! 35 != 30 + 5);
# print(!!35 == 35.0);
# print(!!'35' eq '35.0');
# print(!!'fred' lt 'fred');
# print(!!'fred' lt 'barney');
# print(!!'fred' eq 'fred');
# print(!!'fred' eq 'Fred');
# print(!!' ' eq '');

# my $food = <STDIN>;
# my $betty = chomp $food;
# print $betty;

print("请输入圆的半径:");
my $r = <STDIN>;
if ($r < 0) {
    print(0);
    exit(0)
}
print(2 * 3.141592654 * $r . "\n");

my $x = <STDIN>;
my $y = <STDIN>;
print("$x * $y\n");

my $str = <STDIN>;
my $cnt = <STDIN>;
print($str x $cnt . "\n");