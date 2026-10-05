#pragma once
#include <string>
#include <nlohmann/json.hpp>
// Forward declarations — port_node e Node vivem em local-fingerprint/include/model/
// O .cpp que implementar esses métodos deve incluir os headers completos.
class port_node;
class Node;

class JsonSerializer
{
public:
   template<typename T>
   static std::string toJson(const T& obj, int indent = -1);
};

