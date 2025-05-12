@echo off
color 0a
VBoxManage modifyvm gateway-vm --nictype2 82540EM --nic2 hostonly --hostonlyadapter2 "VirtualBox Host-Only Ethernet Adapter"
color 0f
multipass shell gateway-vm -vvvv