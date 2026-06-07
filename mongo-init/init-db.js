db = db.getSiblingDB('credit_db');

db.getCollection('credit_accounts').insertMany([
    {
        "partnerId": "parceiro-123",
        "balance": NumberDecimal("1500.00"),
        "version": NumberLong(0),
        "updatedAt": new Date()
    },
    {
        "partnerId": "parceiro-master-456",
        "balance": NumberDecimal("50.00"),
        "version": NumberLong(0),
        "updatedAt": new Date()
    }
]);

db.getCollection('partner_transactions').insertMany([
    {
        "partnerId": "parceiro-123",
        "type": "CREDIT",
        "amount": NumberDecimal("1500.00"),
        "description": "Carga inicial de créditos - Ativação via script init",
        "status": "COMPLETED",
        "createdAt": new Date(new Date() - 24*60*60*1000),
        "completedAt": new Date(new Date() - 24*60*60*1000)
    },
    {
        "partnerId": "parceiro-master-456",
        "type": "CREDIT",
        "amount": NumberDecimal("100.00"),
        "description": "Aporte inicial via script init",
        "status": "COMPLETED",
        "createdAt": new Date(new Date() - 5*60*60*1000),
        "completedAt": new Date(new Date() - 5*60*60*1000)
    },
    {
        "partnerId": "parceiro-master-456",
        "type": "DEBIT",
        "amount": NumberDecimal("50.00"),
        "description": "Consumo de API de CEP",
        "status": "COMPLETED",
        "createdAt": new Date(new Date() - 2*60*60*1000),
        "completedAt": new Date(new Date() - 2*60*60*1000)
    }
]);

print("✅ MongoDB populado com sucesso via script de inicialização!");
