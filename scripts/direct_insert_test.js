const db = require('../src/db');
(async()=>{
  try{
    const text = "INSERT INTO usuarios(username, email, password_hash, rol, created_at) VALUES($1,$2,$3,$4,NOW()) RETURNING id";
    const r = await db.query(text,['mobile_direct','mobile_direct@example.com','deadbeef','cliente']);
    console.log('insert ok', r.rows[0]);
  }catch(err){
    console.error('insert err', err && err.stack ? err.stack : err);
  } finally{process.exit(0);} 
})();
