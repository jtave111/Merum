#include "model/Model.h"

void Node::addPort(const port_node& port) {
    
    this->ports.push_back(port);
}

void Node::addVulnerability(const Vulnerability& vuln) {
    vulnerabilities.push_back(vuln);
}

port_node* Node::getOneMutablePort(const int portNumber) {

    for (port_node& p : this->ports) {
        if (p.number == portNumber)
            return &p;
    }
    return nullptr;
}

void Session::addNode(const Node& node) {
    nodes.push_back(node);
}

void Session::addNodes(const std::vector<Node>& newNodes) {

    nodes.insert(nodes.end(), newNodes.begin(), newNodes.end());
}

Node* Session::getNodeFromIpv4(const std::string& ipv4) const
{
    for (const Network_interface& iface: this->interfaces)
    {
        if (iface.ipv4_address == ipv4) return iface.interface_node;

    }
    return nullptr;
}
