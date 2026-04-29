// 创建数据库
use QAssistant

// ==========================================
// 创建 QAssistant_chat_message 集合
// chat_message 将作为嵌套数组嵌入其中
// ==========================================
db.createCollection("QAssistant_chat_message", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["userUid", "createdAt", "updatedAt"],
      properties: {
        _id: {
          bsonType: "objectId",
          description: "主键，自动生成"
        },
        userUid: {
          bsonType: "string",
          description: "用户唯一标识，必填"
        },
        title: {
          bsonType: "string",
          description: "会话标题"
        },
        aiModelId: {
          bsonType: ["long", "null"],
          description: "AI 模型 ID，为 null 时使用系统默认"
        },
        createdAt: {
          bsonType: "date",
          description: "创建时间，必填"
        },
        updatedAt: {
          bsonType: "date",
          description: "更新时间，必填"
        },
        // 嵌套的聊天消息数组
        messages: {
          bsonType: "array",
          description: "嵌套的聊天消息列表",
          items: {
            bsonType: "object",
            required: ["role", "content", "createdAt"],
            properties: {
              _id: {
                bsonType: "objectId",
                description: "消息唯一 ID"
              },
              role: {
                bsonType: "string",
                enum: ["USER", "AI", "SYSTEM"],
                description: "消息角色，枚举值：USER / AI / SYSTEM"
              },
              content: {
                bsonType: "string",
                description: "消息内容"
              },
              createdAt: {
                bsonType: "date",
                description: "消息创建时间，必填"
              }
            }
          }
        }
      }
    }
  }
})

// 按用户 UID 查询会话
db.QAssistant_chat_message.createIndex({ userUid: 1 }, { name: "idx_userUid" })

// 按创建时间倒序排列
db.QAssistant_chat_message.createIndex({ createdAt: -1 }, { name: "idx_createdAt" })

// 复合索引：用户 + 时间
db.QAssistant_chat_message.createIndex(
  { userUid: 1, updatedAt: -1 },
  { name: "idx_userUid_updatedAt" }
)
use QAssistant

db.createCollection("Quiz_paper", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["userUid", "title", "topic", "questions", "totalScore", "createdAt"],
      properties: {

        userUid: {
          bsonType: "string",
          description: "归属用户 UID，必填"
        },

        title: {
          bsonType: "string",
          description: "题卷标题，必填"
        },

        topic: {
          bsonType: "string",
          description: "题目主题，必填"
        },

        difficulty: {
          bsonType: "string",
          enum: ["easy", "medium", "hard"],
          description: "难度等级"
        },

        totalScore: {
          bsonType: "int",
          minimum: 0,
          description: "总分，必填"
        },

        questionCount: {
          bsonType: "int",
          minimum: 0,
          description: "题目总数（冗余字段）"
        },

        choiceCount: {
          bsonType: "int",
          minimum: 0,
          description: "选择题数量（冗余字段）"
        },

        fillCount: {
          bsonType: "int",
          minimum: 0,
          description: "填空题数量（冗余字段）"
        },

        qaCount: {
          bsonType: "int",
          minimum: 0,
          description: "问答题数量（冗余字段）"
        },

        questions: {
          bsonType: "array",
          description: "题目列表，必填",
          items: {
            bsonType: "object",
            required: ["id", "type", "content", "options", "answer", "score"],
            properties: {

              "_id": {
                bsonType: "string",
                description: "题目 ID，如 q1 q2"
              },

              type: {
                bsonType: "string",
                enum: ["choice", "fill", "qa"],
                description: "题目类型"
              },

              content: {
                bsonType: "string",
                description: "题目正文"
              },

              options: {
                bsonType: "array",
                description: "选项列表，fill/qa 时为空数组",
                items: {
                  bsonType: "object",
                  required: ["key", "text"],
                  properties: {
                    key: {
                      bsonType: "string",
                      description: "选项键 A/B/C/D"
                    },
                    text: {
                      bsonType: "string",
                      description: "选项内容"
                    }
                  }
                }
              },

              answer: {
                bsonType: "string",
                description: "答案"
              },

              explanation: {
                bsonType: "string",
                description: "题目解析"
              },

              score: {
                bsonType: "int",
                minimum: 0,
                description: "题目分值"
              }
            }
          }
        },

        sourceFileIds: {
          bsonType: "array",
          description: "RAG 来源文件 ID 列表",
          items: {
            bsonType: "string"
          }
        },

        createdAt: {
          bsonType: "date",
          description: "创建时间，必填"
        },

        updatedAt: {
          bsonType: "date",
          description: "更新时间"
        }
      }
    }
  },
  // 校验失败时的处理策略：error=拒绝写入，warn=写入但打印警告
  validationAction: "error",
  validationLevel: "strict"
})

// ── 创建索引 ────────────────────────────────────────────────

// 1. 按用户查列表 + 时间倒序（最常用查询）
db.Quiz_paper.createIndex(
  { "userUid": 1, "createdAt": -1 },
  { name: "idx_uid_created" }
)

// 2. 按用户 + 难度筛选
db.Quiz_paper.createIndex(
  { "userUid": 1, "difficulty": 1 },
  { name: "idx_uid_difficulty" }
)

// ── 验证结果 ────────────────────────────────────────────────
db.Quiz_paper.getIndexes()