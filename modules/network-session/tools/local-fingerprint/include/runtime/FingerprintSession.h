#pragma once
#include <string>
#include <sstream>
#include <cstdio>
#include <vector>
#include <thread>
#include <mutex>
#include "Ping.h"

class FingerprintSession
{
private:


public:

//Comand exec
std::string comand_exec(std::string comand);


//Binary calc
std::string ipIntToStr(u_int32_t ip);
u_int32_t iptStrToInt(std::string ip);


// Gateway
std::string gatewayIp_module1();

//CIDR & submask
std::string getCidr_module1();
std::string getSubmask_module1();
std::string getNetworkIp_module1();

//NetworkIndentifier
std::string getSSID_module1();
std::string getBSSID_module1();


    /*TODO: criar essa estrutura
        *Layer 1 — Âncoras Estáveis (o que raramente muda)
        ├── ASN (Autonomous System Number)        ← único por empresa/ISP
        ├── IP público de saída                   ← estável em redes corporativas
        └── Domínio DNS reverso do gateway WAN    ← configurado pela empresa
        Layer 2 — Topologia Local (o que define a rede)
            ├── Quantidade de hosts ativos
            ├── Distribuição de portas abertas        ← fingerprint de serviços
            ├── Prefixo de MACs presentes             ← primeiros 3 bytes = vendor OUI
            └── Serviços dominantes (AD, DHCP, DNS)
        Layer 3 — Contexto Físico (opcional, para redes multi-site)
            ├── BSSID (se Wi-Fi)
            └── Localização geográfica aproximada (IP geolocation)
    */
std::string makeIdentifierCombination_module1();



//Mac from ip
std::string getMacAddress_module1(std::string ip);



//Nodes
std::vector<std::string> discoverNodes(std::string ipNet, int cidr);
void addIps(std::string ip, std::vector<std::string>& listIps );



};


