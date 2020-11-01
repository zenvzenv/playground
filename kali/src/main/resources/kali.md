# Kali Linux
zen v

主动嗅探模式
sudo netdiscover -i eth0 -r 192.168.1.0/24
-i device：网卡接口
-r range：需要扫描的范围

被动模式
sudo netdiscover -p
只能监听到网络中主动发送的 arp 包

web压力测试。DOS攻击
hping3 -c 1000 -d 120 -S -w -p 80 --flood -rand-source www.baidu.com
-c 1000 = 发送的数据包数量
-d 120 = 发送到目标主机的每个数据包的大小，单位是字节
-S = 只发送 SYN 数据包
-w 64 = TCP 窗口大小
-p 80 = 目标端口
--flood = 尽可能的发送数据包，不考虑显示入站回复，即洪水攻击
--rand-source = 是·使用随机性的源头IP地址，这里的伪造的IP地址，只是局域网中伪造，经过路由器之后，还是能还原到真实IP的

端口扫描
nmap -sS www.baidu.com
nmap -sS www.baidu.com -p 80
nmap -sS www.baidu.com -p 1-65535
nmap -sS 192.168.1.0/24 -p 1-65535
nmap 192.168.1.53 -sI 192.168.1.63 -p1-1024
-sS = 使用 SYN 进行半连接扫描，速度快

scapy
sr1(ARP("192.168.1.1"))
模拟ping包
sr1(IP(dst="192.168.1.1")/ICMP(),timeout=1)

sr1(IP(dst="192.168.1.1")/TCP(flags="S",dport=80),timeout=1) 
sr1(IP(dst="101.200.128.35")/UDP(dport=80),timeout=1) 

nmap 192.168.1.0/24 -p1-1024 --script=ipidseq.nse

