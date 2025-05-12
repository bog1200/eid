@echo off
color 0a
VBoxManage modifyvm banana-vm --nictype2 82540EM --nic2 hostonly --hostonlyadapter2 "VirtualBox Host-Only Ethernet Adapter"
color 0f
multipass shell banana-vm -vvvv