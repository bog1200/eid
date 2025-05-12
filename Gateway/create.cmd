@echo off
multipass launch -n gateway-vm --cloud-init gateway-user-data.yml --disk 10G -vvvv
multipass stop gateway-vm -vvvv --force
VBoxManage registervm "C:\ProgramData\Multipass\data\virtualbox\vault\instances\gateway-vm\gateway-vm.vbox"
VBoxManage modifyvm gateway-vm --uart1 off
VBoxManage modifyvm gateway-vm --nictype2 virtio --nic2 hostonly --hostonlyadapter2 "VirtualBox Host-Only Ethernet Adapter"
