import http from 'node:http';
import { timingSafeEqual } from 'node:crypto';
const { OPENAI_API_KEY, SUMAX_TOKEN, OPENAI_MODEL, PORT='8080' }=process.env;
if(!OPENAI_API_KEY||!SUMAX_TOKEN||!OPENAI_MODEL) throw new Error('Configure OPENAI_API_KEY, SUMAX_TOKEN and OPENAI_MODEL');
const send=(res,status,data)=>{res.writeHead(status,{'Content-Type':'application/json','Cache-Control':'no-store'});res.end(JSON.stringify(data));};
const modes={Hunter:'Be analytical and practical.',Shadow:'Be concise and strategic.',Guardian:'Be supportive and practical.'};
http.createServer(async(req,res)=>{
 if(req.method==='GET'&&req.url==='/health')return send(res,200,{ok:true});
 if(req.method!=='POST'||req.url!=='/chat')return send(res,404,{error:'Not found'});
 const a=Buffer.from(req.headers.authorization||''),b=Buffer.from('Bearer '+SUMAX_TOKEN);
 if(a.length!==b.length||!timingSafeEqual(a,b))return send(res,401,{error:'Unauthorized'});
 try{
 let size=0;const chunks=[];for await(const chunk of req){size+=chunk.length;if(size>16384)return send(res,413,{error:'Too large'});chunks.push(chunk);}
 const {message,mode}=JSON.parse(Buffer.concat(chunks).toString());
 if(typeof message!=='string'||!message.trim()||message.length>4000||!Object.hasOwn(modes,mode))return send(res,400,{error:'Invalid input'});
 const upstream=await fetch('https://api.openai.com/v1/chat/completions',{method:'POST',headers:{Authorization:'Bearer '+OPENAI_API_KEY,'Content-Type':'application/json'},body:JSON.stringify({model:OPENAI_MODEL,messages:[{role:'system',content:'You are SUMAX, a helpful assistant. '+modes[mode]},{role:'user',content:message}],max_completion_tokens:500}),signal:AbortSignal.timeout(20000)});
 if(!upstream.ok)return send(res,502,{error:'AI provider unavailable'});
 const data=await upstream.json();const reply=data.choices?.[0]?.message?.content;
 if(typeof reply!=='string'||!reply.trim())return send(res,502,{error:'Empty response'});
 return send(res,200,{reply});
 }catch{return send(res,400,{error:'Request failed'});}
}).listen(Number(PORT),'0.0.0.0');
