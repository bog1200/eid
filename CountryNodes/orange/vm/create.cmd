@echo off
@echo off
multipass launch -n orange-vm --cloud-init orange-user-data.yml --disk 30G --cpus 2 --memory 2G -vvvv
multipass stop orange-vm -vvvv --force
VBoxManage registervm "C:\ProgramData\Multipass\data\virtualbox\vault\instances\orange-vm\orange-vm.vbox"
VBoxManage modifyvm orange-vm --uart1 off
VBoxManage modifyvm orange-vm --nictype2 virtio --nic2 hostonly --hostonlyadapter2 "VirtualBox Host-Only Ethernet Adapter"
