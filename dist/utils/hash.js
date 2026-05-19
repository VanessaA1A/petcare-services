"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.hashPassword = hashPassword;
const crypto_1 = __importDefault(require("crypto"));
function hashPassword(password) {
    return crypto_1.default.createHash('md5').update(password, 'utf8').digest('hex');
}
