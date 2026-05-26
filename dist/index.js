"use strict";
var __createBinding = (this && this.__createBinding) || (Object.create ? (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    var desc = Object.getOwnPropertyDescriptor(m, k);
    if (!desc || ("get" in desc ? !m.__esModule : desc.writable || desc.configurable)) {
      desc = { enumerable: true, get: function() { return m[k]; } };
    }
    Object.defineProperty(o, k2, desc);
}) : (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    o[k2] = m[k];
}));
var __setModuleDefault = (this && this.__setModuleDefault) || (Object.create ? (function(o, v) {
    Object.defineProperty(o, "default", { enumerable: true, value: v });
}) : function(o, v) {
    o["default"] = v;
});
var __importStar = (this && this.__importStar) || (function () {
    var ownKeys = function(o) {
        ownKeys = Object.getOwnPropertyNames || function (o) {
            var ar = [];
            for (var k in o) if (Object.prototype.hasOwnProperty.call(o, k)) ar[ar.length] = k;
            return ar;
        };
        return ownKeys(o);
    };
    return function (mod) {
        if (mod && mod.__esModule) return mod;
        var result = {};
        if (mod != null) for (var k = ownKeys(mod), i = 0; i < k.length; i++) if (k[i] !== "default") __createBinding(result, mod, k[i]);
        __setModuleDefault(result, mod);
        return result;
    };
})();
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
require("dotenv/config");
const express_1 = __importDefault(require("express"));
const path_1 = __importDefault(require("path"));
const body_parser_1 = __importDefault(require("body-parser"));
const users_1 = __importDefault(require("./routes/users"));
const auth_1 = __importDefault(require("./routes/auth"));
const pets_1 = __importDefault(require("./routes/pets"));
const sessionAuth_1 = __importDefault(require("./middleware/sessionAuth"));
const authController = __importStar(require("./controllers/authController"));
const usersController = __importStar(require("./controllers/usersController"));
const db_1 = require("./db");
const app = (0, express_1.default)();
const port = process.env.PORT || 3000;
app.use(body_parser_1.default.json());
app.use(express_1.default.static(path_1.default.join(__dirname, '../public')));
app.get('/', (req, res) => res.sendFile(path_1.default.join(__dirname, '../public/login.html')));
app.get('/login', (req, res) => res.sendFile(path_1.default.join(__dirname, '../public/login.html')));
app.post('/login', authController.login);
app.get('/register', (req, res) => res.sendFile(path_1.default.join(__dirname, '../public/register.html')));
app.post('/register', usersController.createUser);
app.get('/profile', sessionAuth_1.default, authController.me);
app.get('/health', (req, res) => res.json({ status: 'ok' }));
app.use('/api/users', users_1.default);
app.use('/api/auth', auth_1.default);
app.use('/api/pets', pets_1.default);
async function start() {
    try {
        console.log('Checking database connectivity...');
        const r = await (0, db_1.query)('SELECT 1 as ok');
        if (!r || !r.rows)
            throw new Error('No response from DB');
        console.log('Database OK:', r.rows[0]);
        app.listen(port, () => {
            console.log(`PetCare services listening on port ${port}`);
        });
    }
    catch (err) {
        console.error('Failed to start application. DB connectivity error:', err.message || err);
        process.exit(1);
    }
}
start();
