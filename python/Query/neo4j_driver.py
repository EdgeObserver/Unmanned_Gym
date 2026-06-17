# neo4j_driver.py
from neo4j import GraphDatabase
import os

class Neo4jConnection:
    def __init__(self, uri, user, password):
        self.driver = GraphDatabase.driver(uri, auth=(user, password))

    def close(self):
        self.driver.close()

    def query(self, query, parameters=None):
        with self.driver.session() as session:
            result = session.run(query, parameters)
            return [record.data() for record in result]

# 初始化连接（根据你的配置修改）
neo4j_conn = Neo4jConnection(
    uri="bolt://localhost:7687",
    user="neo4j",
    password="7980nospo"  # ← 改成你的密码！
)