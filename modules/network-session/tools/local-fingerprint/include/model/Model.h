#pragma once
#include <string>
#include <vector>

struct Session;
struct Network_interface;
struct Node;

enum port_status{

    CLOSED = 0,
    OPEN = 1,
    FILTERED = 2,
    OPEN_FILTERED = 3,
    INTERNAL_ERROR = 4
};

struct port_node {
    int  number = 0;
    std::string protocol;
    std::string service;
    std::string banner;
    std::string status;
};

struct Vulnerability {
    Node * node_vulnerability = nullptr;
    std::string cve;
    std::string name;
    std::string title;
    std::string severity;
};

struct Node {

    std::string hostname;
    std::string vendor;
    Session *session = nullptr;
    std::vector<Network_interface> interfaces;
    int vulnerabilityScore = 0;
    std::vector< struct port_node> ports;
    std::vector<Vulnerability> vulnerabilities;
    void  addPort(const struct port_node& port);
    void  addVulnerability(const Vulnerability& vuln);
    port_node* getOneMutablePort(int portNumber);
};

struct Network_interface
{
    Node * gateway_node = nullptr;
    Node * interface_node = nullptr;
    std::string ipv4_address;
    std::string ipv6_address;
    std::string mac_address;
    std::string subnet_mask;
    bool dhcp_enabled = false;
    bool isVirtual = false;
    std::string cidr;
};
struct Session {

    //TODO: pensar em uma identificação geral da rede
    std::string network_identifier;
    std::vector<Node> nodes;
    std::vector<Network_interface> interfaces;

    std::vector<Node> & getMutableNodes();
    void   addNode(const Node& node);
    void   addNodes(const std::vector<Node>& newNodes);
    Node*  getNodeFromIpv4(const std::string& ipv4) const;
};
